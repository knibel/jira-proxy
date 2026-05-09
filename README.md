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
- `JIRA_WEBHOOK_ENABLED`: enable Jira webhook processing (`true`/`false`)
- `JIRA_WEBHOOK_SECRET`: optional shared secret expected in `X-Jira-Webhook-Secret`
- `JIRA_WEBHOOK_CALLBACK_URL`: URL that receives notification payloads for matching events

## API

- `GET /api/jira/tickets?jql=<JQL>`
- `GET /api/jira/tickets/{issueKey}/comments`
- `POST /api/jira/tickets/{issueKey}/comments` with JSON body `{ "body": "text" }`
- `POST /webhooks/jira` for Jira webhook events (no JWT required)

## Webhook notifications for User Stories

You can get notified when a Jira **Story/User Story** changes assignment or labels:

1. Set `JIRA_WEBHOOK_ENABLED=true`.
2. Set `JIRA_WEBHOOK_CALLBACK_URL` to your notification endpoint.
3. Optionally set `JIRA_WEBHOOK_SECRET` and configure Jira to send the same value in `X-Jira-Webhook-Secret`.
4. Configure a Jira webhook that targets this service at `POST /webhooks/jira` and includes issue updates.

The proxy filters incoming Jira webhook payloads and only forwards notifications when:
- issue type is `Story` or `User Story`
- changelog includes `assignee` or `labels` changes
