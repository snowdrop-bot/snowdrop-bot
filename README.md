# snowdrop-bot
A bot for automating snowdrop related tasks

## Features

- Issues tracking across multiple organizations and repositories
- Pull request tracking across multiple organization and repositories
- Forked repository issue bridging.


## Installation

### Database

For the persistence needs of this application `h2` has been used.
The database is configured to store files under `/data/snowdrop-bot/db`.
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
`credentials.json` file under `/data/snowdrop-bot/google/credentials.json`.
Again the location is configurable via `google.docs.credentials.file` property.
