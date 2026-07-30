# Release guide

TestCaseDroid publishes separate CLI and Java Swing GUI editions. Each edition
has a thin JAR (dependencies external) and an all-in-one JAR (dependencies
embedded).

## Automated release

1. Update the project version in `pom.xml`.
2. Merge the change into the commit that should be released.
3. Create and push a matching annotated tag:

   ```bash
   git tag -a v1.4.0 -m "TestCaseDroid 1.4.0"
   git push origin v1.4.0
   ```

The `Release` workflow checks that tag `vX.Y.Z` matches the Maven version,
runs the full test suite, smoke-tests all four JAR entry points and the AI
report exporter, creates portable archives, generates SHA-256 checksums and a
signed GitHub artifact attestation, then creates the GitHub Release.

An existing tag can be republished from **Actions → Release → Run workflow**.
The manual input must name a tag that already exists.

## Release assets

| Asset | Entry point | Dependencies |
| --- | --- | --- |
| `TestCaseDroid-X.Y.Z-cli.jar` | CLI | External `lib/` directory |
| `TestCaseDroid-X.Y.Z-cli-all.jar` | CLI | Embedded |
| `TestCaseDroid-X.Y.Z-gui.jar` | GUI launcher | External `lib/` directory |
| `TestCaseDroid-X.Y.Z-gui-all.jar` | GUI launcher | Embedded |
| `TestCaseDroid-X.Y.Z-dependencies.zip` | Shared by both thin JARs | Contains `lib/` |
| `TestCaseDroid-X.Y.Z-cli-portable.*` | CLI launcher scripts | Embedded in bundled JAR |
| `TestCaseDroid-X.Y.Z-gui-portable.*` | GUI launcher scripts | Embedded in bundled JAR |
| `SHA256SUMS.txt` | Verification | All release binaries |

For the smallest download with no dependency management, choose `cli-all.jar`
or `gui-all.jar`. To use a thin JAR, extract
`TestCaseDroid-X.Y.Z-dependencies.zip` beside it so the layout is:

```text
TestCaseDroid-X.Y.Z-cli.jar
lib/
├── soot-*.jar
└── ...
```

Verify a checksum:

```bash
sha256sum -c SHA256SUMS.txt
```

Verify GitHub build provenance:

```bash
gh attestation verify TestCaseDroid-X.Y.Z-cli-all.jar \
  --repo NiceAsiv/TestCaseDroid
```

## Required repository settings

GitHub Actions must be enabled and workflow permissions must allow
`GITHUB_TOKEN` to create releases. The workflow declares only the permissions
needed for release uploads and artifact attestations; no repository secret is
required.
