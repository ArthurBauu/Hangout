Firebase Test Lab — configuration rapide pour GitHub Actions

But: this file explains how to create a service account and add the required GitHub secrets so the `firebase_test` job in `.github/workflows/android-ci.yml` can run.

Steps (summary):

1) Enable required APIs for your GCP project

Run locally (replace PROJECT_ID):

```
gcloud config set project PROJECT_ID
gcloud services enable testing.googleapis.com firebase.googleapis.com
```

2) Create a service account for GitHub Actions

```
gcloud iam service-accounts create github-actions-firebase \
  --display-name="GitHub Actions Firebase Test Lab"
```

3) Grant roles to the service account

Grant the minimal roles needed to run tests and upload results. Replace PROJECT_ID accordingly.

```
gcloud projects add-iam-policy-binding PROJECT_ID \
  --member="serviceAccount:github-actions-firebase@PROJECT_ID.iam.gserviceaccount.com" \
  --role="roles/firebase.testLabAdmin"

# Optionally allow access to storage/test results if needed
gcloud projects add-iam-policy-binding PROJECT_ID \
  --member="serviceAccount:github-actions-firebase@PROJECT_ID.iam.gserviceaccount.com" \
  --role="roles/storage.objectAdmin"
```

4) Create a JSON key and keep it safe

```
gcloud iam service-accounts keys create key.json \
  --iam-account=github-actions-firebase@PROJECT_ID.iam.gserviceaccount.com
```

5) Add GitHub secrets

- `GCP_SA_KEY`: the **entire JSON key file** content (copy/paste).
- `GCP_PROJECT`: your GCP project ID.

Use the GitHub web UI (Settings → Secrets → Actions) or the GH CLI:

```
gh secret set GCP_SA_KEY --body "$(cat key.json)"
gh secret set GCP_PROJECT --body "PROJECT_ID"
```

Important: remove `key.json` from your machine after adding the secret:

```
shred -u key.json || rm -f key.json
```

6) Run the manual job

Go to Actions → select the workflow and click "Run workflow" (choose branch `main`). The `firebase_test` job runs using the secrets.

Notes & troubleshooting

- The action uses `gcloud` under the `google-github-actions/setup-gcloud@v1` step — ensure the service account JSON is valid.
- If tests fail due to device/model availability or quotas, try another `model` or `version` in the `gcloud` command (e.g., `model=Pixel5`).
- You can restrict the service account key and rotate/delete it via GCP IAM when no longer needed.

If you want, I can generate a short `docs/` page with screenshots for the GitHub Secrets UI and the exact GCP Console navigation steps.