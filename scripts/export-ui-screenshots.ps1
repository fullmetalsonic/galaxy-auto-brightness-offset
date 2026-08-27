param(
    [switch]$Check,
    [switch]$PublicOnly,
    [string]$SourceDirectory = (Join-Path $PSScriptRoot '..\build-evidence'),
    [string]$ManifestPath = (Join-Path $PSScriptRoot '..\docs\images\ui-screenshots-v1.4.1.json')
)

$ErrorActionPreference = 'Stop'
$manifest = Get-Content -LiteralPath $ManifestPath -Raw | ConvertFrom-Json
$outputDirectory = Split-Path (Resolve-Path -LiteralPath $ManifestPath).Path -Parent

# This branch is cross-platform and never needs private captures or System.Drawing.
if ($PublicOnly) {
    $projectRoot = Split-Path $PSScriptRoot -Parent
    $buildFile = Get-Content -LiteralPath (Join-Path $projectRoot 'app/build.gradle.kts') -Raw
    if ($buildFile -notmatch ('versionName\s*=\s*"' + [regex]::Escape($manifest.appVersion) + '"')) {
        throw 'Screenshot manifest version does not match the Android app.'
    }
    foreach ($doc in @('design-qa.md', 'docs/USER_GUIDE_KO.md', 'docs/USER_GUIDE_EN.md',
        "docs/RELEASE_NOTES_v$($manifest.appVersion).md")) {
        $title = Get-Content -LiteralPath (Join-Path $projectRoot $doc) -TotalCount 1
        if ($title -notmatch [regex]::Escape("v$($manifest.appVersion)")) {
            throw "Current UI documentation title is stale: $doc"
        }
    }
    foreach ($item in $manifest.images) {
        if ($item.file -notmatch '^[a-z0-9.-]+\.png$' -or
            $item.outputSha256 -notmatch '^[A-Fa-f0-9]{64}$' -or
            $item.sourceSha256 -notmatch '^[A-Fa-f0-9]{64}$') {
            throw 'Invalid public screenshot metadata.'
        }
        $path = Join-Path $outputDirectory $item.file
        if ((Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash -ne $item.outputSha256) {
            throw "Output hash mismatch: $($item.file)"
        }
        $bytes = [IO.File]::ReadAllBytes($path)
        if ($bytes.Length -lt 24 -or
            [BitConverter]::ToString($bytes, 0, 8) -ne '89-50-4E-47-0D-0A-1A-0A') {
            throw "Not a PNG: $($item.file)"
        }
        $width = ([int]$bytes[16] -shl 24) -bor ([int]$bytes[17] -shl 16) -bor ([int]$bytes[18] -shl 8) -bor [int]$bytes[19]
        $height = ([int]$bytes[20] -shl 24) -bor ([int]$bytes[21] -shl 16) -bor ([int]$bytes[22] -shl 8) -bor [int]$bytes[23]
        if ($width -ne $item.crop.width -or $height -ne $item.crop.height) {
            throw "Output size mismatch: $($item.file)"
        }
        Write-Output "$($item.file): PASS, public hash and dimensions"
    }
    Write-Output "UI documentation version: v$($manifest.appVersion) PASS"
    return
}

Add-Type -AssemblyName System.Drawing
if (-not ('ScreenshotPixelComparison' -as [type])) {
    Add-Type @'
public static class ScreenshotPixelComparison {
    public static long DifferenceCount(byte[] expected, byte[] actual) {
        if (expected.Length != actual.Length) return -1;
        long count = 0;
        for (int i = 0; i < expected.Length; i += 4) {
            if (expected[i] != actual[i] || expected[i + 1] != actual[i + 1] ||
                expected[i + 2] != actual[i + 2] || expected[i + 3] != actual[i + 3]) count++;
        }
        return count;
    }
}
'@
}

function Get-ScreenshotPixels([System.Drawing.Bitmap]$Bitmap) {
    $rect = [System.Drawing.Rectangle]::new(0, 0, $Bitmap.Width, $Bitmap.Height)
    $data = $Bitmap.LockBits($rect, [System.Drawing.Imaging.ImageLockMode]::ReadOnly,
        [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    try {
        $rowLength = $Bitmap.Width * 4
        $bytes = [byte[]]::new($rowLength * $Bitmap.Height)
        for ($y = 0; $y -lt $Bitmap.Height; $y++) {
            [System.Runtime.InteropServices.Marshal]::Copy(
                [IntPtr]::Add($data.Scan0, $y * $data.Stride),
                $bytes, $y * $rowLength, $rowLength)
        }
        return ,$bytes
    }
    finally { $Bitmap.UnlockBits($data) }
}

$sourceRoot = (Resolve-Path -LiteralPath $SourceDirectory).Path

foreach ($item in $manifest.images) {
    # Accept basenames only: private capture paths must never enter public metadata.
    if ($item.source -ne [IO.Path]::GetFileName($item.source) -or
        $item.file -ne [IO.Path]::GetFileName($item.file) -or
        $item.source -notmatch '\.png$' -or $item.file -notmatch '\.png$') {
        throw 'The screenshot manifest must contain PNG basenames only.'
    }
    $sourcePath = Join-Path $sourceRoot $item.source
    $outputPath = Join-Path $outputDirectory $item.file
    if ((Get-FileHash -LiteralPath $sourcePath -Algorithm SHA256).Hash -ne $item.sourceSha256) {
        throw "Source hash mismatch: $($item.source)"
    }
    $source = [System.Drawing.Bitmap]::new($sourcePath)
    $cropped = $null
    try {
        $c = $item.crop
        if ($c.x -lt 0 -or $c.y -lt 0 -or $c.width -le 0 -or $c.height -le 0 -or
            $c.x + $c.width -gt $source.Width -or $c.y + $c.height -gt $source.Height) {
            throw "Crop is outside the source: $($item.file)"
        }
        $rect = [System.Drawing.Rectangle]::new($c.x, $c.y, $c.width, $c.height)
        # Direct pixel crop only: no painting, resizing, masking, or generative editing.
        $cropped = $source.Clone($rect, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        if (-not $Check) { $cropped.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png) }
        $published = [System.Drawing.Bitmap]::new($outputPath)
        try {
            if ($published.Width -ne $c.width -or $published.Height -ne $c.height) {
                throw "Output size mismatch: $($item.file)"
            }
            $difference = [ScreenshotPixelComparison]::DifferenceCount(
                (Get-ScreenshotPixels $cropped), (Get-ScreenshotPixels $published))
            if ($difference -ne 0) { throw "$($item.file): $difference changed pixels" }
        }
        finally { $published.Dispose() }
        $hash = (Get-FileHash -LiteralPath $outputPath -Algorithm SHA256).Hash
        if ($item.outputSha256 -and $hash -ne $item.outputSha256) {
            throw "Output hash mismatch: $($item.file)"
        }
        Write-Output "$($item.file): PASS, $($c.width)x$($c.height), changed pixels=0, SHA256=$hash"
    }
    finally {
        if ($cropped) { $cropped.Dispose() }
        $source.Dispose()
    }
}
