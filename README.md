# jira-proxy

Spring Boot REST proxy for Jira.

## Features

- Search for Jira tickets via JQL
- Read comments for an issue
- Add comments to an issue
- Secure API endpoints with OAuth2/OIDC JWT bearer tokens (client credentials flow)
- Call Jira with configured username/password (basic auth)

## Configuration

Set these properties as environment variables:

- `OIDC_ISSUER_URI`: OIDC issuer used to validate incoming JWT access tokens
- `JIRA_BASE_URL`: Jira base URL (for example `https://your-jira-instance.atlassian.net`)
- `JIRA_USERNAME`: Jira technical user name/email
- `JIRA_PASSWORD`: Jira password or API token

## API

- `GET /api/jira/tickets?jql=<JQL>`
- `GET /api/jira/tickets/{issueKey}/comments`
- `POST /api/jira/tickets/{issueKey}/comments` with JSON body `{ "body": "text" }`
