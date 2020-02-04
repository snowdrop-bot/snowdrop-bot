# Snowdrop Bot
A bot for automating snowdrop related tasks

## Features

- [Issues tracking](#issue-tracking) across multiple organizations and repositories
- [Pull request tracking](#pull-request-tracking) across multiple organization and repositories
- [Forked repository issue bridging](#forked-repository-issue-bridging)
- [Google Docs report generation](#google-docs-report-generation)

## Installation

### Database

For the persistence needs of this application `h2` has been used.
The database is configured to store files under `~/.snowdrop-bot/db`.
So you will either need to create the folder and ensure that you have write
access, or reconfigure the application to store files somewhere else.

You can specify where h2 will store files by editing: `quarkus.datasource.url`.

### Github

The application will need to access github. So you are going to need an access
token.
The token can be configured either via `github.token` property or `GITHUB_TOKEN`
environment variable.

### Google APIS

To upload generate reports to Google Documents, you will need to enable the
Google Documents API. Once enabled, you will need to store the provided
`credentials.json` file under `/.snowdrop-bot/google/credentials.json`.
Again the location is configurable via `google.docs.credentials.file` property.

## Running

To run the bot you can just build:

    mvn clean install
    
and then run with:

    java -jar target/snowdrop-bot-0.1-SNAPSHOT-runner.jar

*NOTE:* The `github.token` is required, so either set it in
`application.properites` or create an environment variable for it.

### Default Profile
To avoid unnecessary trafic on the github API (which is subject of rate
limiting) the default profile has the core features (e.g. bridge and reporting disabled).

You can enable them selectively through the UI, or use a different profile (e.g.
production).

#### Persistence and configuration (defalut)
The default profile uses /data/snowdrop-bot as the root for configuration and database files.


### Production Profile
The production profile has all features enabled by default.
Additionally, everything related to file persistence or file configuration is
configured under `/data/snowdrop-bot` which which is a volume friendlier path
(e.g. when running on Kubernetes).

#### Persistence and configuration (production)
The production profile uses /data/snowdrop-bot as the root for configuration and database files.

## Issue tracking

Provides a unified view of all `active` issues across multiple repositories.
The criteria for selecting issues are the following:

- exist in repositories of configured organizations (`github.reporting.organizations`)
- assigned to configured users (`github.users`)
- the assigned user has forked the repository 
- were open within the configured time frame (the week bounded by `github.reporting.day-of-week` & `github.reporting.hours`)

Those issues, can be exported in csv, excel or pdf.

**NOTE:** At the moment we don't track the activity of the issue in any way. In
other words, we do not filter out `stale` issues.

![issues screen](./img/issues.png "Issues Screen")

## Pull Request tracking

Provides a unified view of all `active` pull requests across multiple repositories.
The criteria for selecting issues are the following:

- exist in repositories of configured organizations (`github.reporting.organizations`)
- assigned to configured users (`github.users`)
- the assigned user has forked the repository 
- were open within the configured time frame (the week bounded by `github.reporting.day-of-week` & `github.reporting.hours`)

![pull request screen](./img/pull-requests.png "Pull Requests Screen")

Those pull requests, can be exported in csv, excel or pdf.

**NOTE:** At the moment we don't track the activity of the pull requests in any way. In
other words, we do not filter out `stale` pull requests.

## Forked repository issue bridging

When working across multiple different organizations owned by different teams
with variable access levels, its important to be able to have an aggregated view
of the pending issues. This is important for things like:

- tracking 
- scheduling
- reporting

Lack of permissions in 3rd party repositories make it difficult to manage and
aggregate. In other cases its not allowed to track repositories across
organizations.

Whatever, the reason forking the repository and using a bridge to `clone` issues
of interest is a possible solution.

This bot provides this feature and allows you to configure the following
options:

- source repositories (`github.bridge.source-repos`)
- target organization (`github.bridge.target-organization`)
- terminal label (the label to use to mark an issue as closed `github.bridge.terminal-label`)


## Google Docs Report Generation

It's possible to combine information found in `issues` and `pull requests` and
present them in `Google Docs`.

This can be done by selecting the `Reporting` tab and clicking the `generate` button.

This requires the setup mentioned in [Google APIS](#google-apis).
The id of the target document can be specified using `google.docs.report.document-id`.
