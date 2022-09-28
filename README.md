# Jenkins Workflow Extensions

[![ci](https://github.com/jhnc-oss/jenkins-workflow-extensions/actions/workflows/ci.yml/badge.svg)](https://github.com/jhnc-oss/jenkins-workflow-extensions/actions/workflows/ci.yml)
[![GitHub release](https://img.shields.io/github/release/jhnc-oss/jenkins-workflow-extensions.svg)](https://github.com/jhnc-oss/jenkins-workflow-extensions/releases)
[![License](https://img.shields.io/badge/license-MIT-yellow.svg)](LICENSE)
![Java](https://img.shields.io/badge/java-11-green.svg)

Extensions for Jenkins Workflow Pipelines.

## Multibranch build blocking

Multibranch Builds can be blocked on a project or job level. Triggered builds get queued until unblocked.

### Permissions

The `Item/Configure` permission is required to block jobs.

## Disable branch build strategy

Branch indexing remains enabled, but no builds are started on changes. This keeps the Mulitbranch projects updated without triggering builds automatically.
