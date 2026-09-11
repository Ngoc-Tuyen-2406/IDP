[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateScript({ Test-Path -LiteralPath $_ -PathType Container })]
    [string]$SourceRoot,

    [switch]$Force,

    [string[]]$DatasetIds
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$rawRoot = Join-Path $projectRoot 'datasets\\raw'
$sevenZip = 'C:\\Program Files\\7-Zip\\7z.exe'

if (-not (Test-Path -LiteralPath $sevenZip -PathType Leaf)) {
    throw '7-Zip is required to stage these archives on Windows. Install 7-Zip or update the script path.'
}

$datasets = @(
    @{ Id = 'contract_nli'; Archive = 'contract-nli.zip' },
    @{ Id = 'signature_rf100'; Archive = 'signatures.v2-release.yolov11.zip' },
    @{ Id = 'signature_roboflow_amruth'; Archive = 'Signature Detection.v2i.yolov11.zip' },
    @{ Id = 'doclaynet_reference'; Archive = 'DocLayNet-main.zip' },
    @{ Id = 'ndl_docl_sample'; Archive = 'layout-dataset-master.zip' },
    @{ Id = 'pubtables_reference'; Archive = 'table-transformer-main.zip' },
    @{ Id = 'vietnamese_ocr_archive'; Archive = 'archive.zip' }
)

New-Item -ItemType Directory -Force -Path $rawRoot | Out-Null

foreach ($dataset in $datasets) {
    if ($DatasetIds -and $dataset.Id -notin $DatasetIds) {
        continue
    }

    $archivePath = Join-Path $SourceRoot $dataset.Archive
    $destination = Join-Path $rawRoot $dataset.Id
    $completionMarker = Join-Path $destination '.staging-complete'
    $temporaryDestination = Join-Path $rawRoot ('.staging-' + $dataset.Id)

    if (-not (Test-Path -LiteralPath $archivePath -PathType Leaf)) {
        Write-Warning "Archive not found: $archivePath"
        continue
    }

    if (Test-Path -LiteralPath $destination) {
        if (Test-Path -LiteralPath $completionMarker) {
            Write-Host "Skipped completed dataset: $($dataset.Id)"
            continue
        }
        if (-not $Force) {
            Write-Warning "Partial dataset found: $($dataset.Id). Re-run with -Force to replace only this staging directory."
            continue
        }
        if ($Force) {
            Remove-Item -LiteralPath $destination -Recurse -Force
        }
    }

    if (Test-Path -LiteralPath $temporaryDestination) {
        Remove-Item -LiteralPath $temporaryDestination -Recurse -Force
    }

    New-Item -ItemType Directory -Force -Path $temporaryDestination | Out-Null
    Write-Host "Extracting $($dataset.Archive) -> $destination"

    try {
        # 7-Zip supports archive entry names that Windows/.NET rejects, including
        # ContractNLI's percent-encoded source filenames.
        & $sevenZip 'x' '-y' ("-o$temporaryDestination") $archivePath | Out-Host
        if ($LASTEXITCODE -ne 0) {
            throw "7-Zip failed with exit code $LASTEXITCODE for $($dataset.Archive)."
        }
        Move-Item -LiteralPath $temporaryDestination -Destination $destination
        New-Item -ItemType File -Path $completionMarker | Out-Null
    }
    catch {
        if (Test-Path -LiteralPath $temporaryDestination) {
            Remove-Item -LiteralPath $temporaryDestination -Recurse -Force
        }
        throw
    }
}

Write-Host 'Dataset staging completed. Review datasets/registry/DATA_REGISTER.csv before training.'
