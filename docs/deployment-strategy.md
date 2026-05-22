# Deployment Strategy

The catalogue service follows BidMart's staging-first progressive promotion strategy.

## Environment Mapping

| Branch | Environment | Platform |
| --- | --- | --- |
| `staging` | Staging | VPS Docker Compose through `bidmart-infrastructure` |
| `main` | Production | VPS Docker Compose through `bidmart-infrastructure` |

## Gate

The service dispatches VPS deployment only after the `Continuous Integration` workflow succeeds. Failed tests or failed quality checks stop deployment before the infrastructure repo is notified.

## Promotion Flow

1. Merge catalogue changes into `staging`.
2. CI validates the service.
3. Successful CI dispatches staging deployment in `bidmart-infrastructure`.
4. Validate catalogue behavior through gateway and monitoring.
5. Promote the change to `main`.
6. CI success on `main` dispatches production deployment.

## Rollback

Rollback is branch-based: revert on `main`, wait for CI, and let the production deployment redeploy the previous known-good catalogue state.
