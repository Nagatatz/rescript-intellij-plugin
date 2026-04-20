```bash
gcloud builds submit --tag gcr.io/PROJECT-ID/{{projectName}}
gcloud run deploy {{projectName}} \\
  --image gcr.io/PROJECT-ID/{{projectName}} \\
  --port 8080 \\
  --allow-unauthenticated
```