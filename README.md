# Naukri Profile Auto-Updater

Automatically refreshes your Naukri profile's "Last Updated" timestamp every day using Selenium + Java, deployed as a free GitHub Actions scheduled workflow.

**Why this works:** Naukri's recruiter search sorts profiles by last updated date. A daily save (with no actual content changes) pushes your profile to the top of search results, increasing recruiter visibility.

---

## Project structure

```
naukri-automation/
├── pom.xml                                          Maven build file
├── src/main/java/com/naukri/automation/
│   └── NaukriProfileUpdater.java                    Main automation script
├── src/main/resources/
│   └── logback.xml                                  Logging config
└── .github/workflows/
    └── naukri-refresh.yml                           GitHub Actions cron schedule
```

---

## Quick setup (15 minutes)

### Step 1 — Fork or push this project to GitHub

Create a new **private** GitHub repository (keep your code private).

```bash
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/YOUR_USERNAME/naukri-automation.git
git push -u origin main
```

> Use a **private** repo so your workflow file and code are not publicly visible.

---

### Step 2 — Add your credentials as GitHub Secrets

Your email and password are **never stored in code**. They are stored as encrypted GitHub Secrets.

1. Go to your repository on GitHub
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret** and add these two secrets:

| Secret name       | Value                        |
|-------------------|------------------------------|
| `NAUKRI_EMAIL`    | your.email@example.com       |
| `NAUKRI_PASSWORD` | your_naukri_password         |

---

### Step 3 — Enable GitHub Actions

1. Click the **Actions** tab in your repository
2. If prompted, click **I understand my workflows, go ahead and enable them**
3. You should see the **Naukri Daily Profile Refresh** workflow listed

---

### Step 4 — Run it manually first (test)

Before waiting for the daily schedule:

1. Go to **Actions** → **Naukri Daily Profile Refresh**
2. Click **Run workflow** → **Run workflow**
3. Watch the logs in real time

A successful run looks like:
```
08:00:01 [INFO] === Naukri Profile Updater started at 2024-03-15T08:00:01 ===
08:00:02 [INFO] Setting up headless Chrome...
08:00:04 [INFO] Chrome driver ready.
08:00:04 [INFO] Navigating to login page...
08:00:08 [INFO] Login submitted. Waiting for home page...
08:00:12 [INFO] Logged in. Current URL: https://www.naukri.com/mnjuser/homepage
08:00:15 [INFO] Navigating to profile page...
08:00:19 [INFO] Opened resume headline edit widget.
08:00:21 [INFO] Looking for Save button...
08:00:23 [INFO] Clicked Save button.
08:00:25 [INFO] Save confirmation detected.
08:00:25 [INFO] ✅  Profile updated successfully. Timestamp refreshed on Naukri.
08:00:25 [INFO] Browser closed.
```

---

## Schedule

The workflow runs daily at **08:00 AM IST** (02:30 UTC).

To change the time, edit `.github/workflows/naukri-refresh.yml`:

```yaml
schedule:
  - cron: '30 2 * * *'   # minute hour day month weekday (UTC)
```

Some useful IST times converted to UTC (IST = UTC + 5:30):

| Run at (IST) | Cron (UTC)     |
|--------------|----------------|
| 7:00 AM      | `30 1 * * *`   |
| 8:00 AM      | `30 2 * * *`   |
| 9:00 AM      | `30 3 * * *`   |
| 6:00 PM      | `30 12 * * *`  |

---

## Running locally (optional)

```bash
# Set credentials in your terminal
export NAUKRI_EMAIL="your.email@example.com"
export NAUKRI_PASSWORD="your_password"

# Build
mvn package -DskipTests

# Run
java -jar target/naukri-automation-1.0.0.jar
```

You will need Google Chrome installed locally. WebDriverManager downloads ChromeDriver automatically.

---

## Troubleshooting

### "Could not find Save button"
Naukri occasionally updates their HTML/CSS. The CSS selectors in `NaukriProfileUpdater.java` may need updating.

1. Open Naukri in your browser
2. Log in and go to your profile
3. Open DevTools (F12) → right-click the Save button → Inspect
4. Update `SEL_SAVE_BUTTON` or `SEL_HEADLINE_EDIT` in the Java file

### OTP / CAPTCHA triggered
If Naukri sends an OTP to your phone, the automation cannot proceed. This happens when:
- You log in from a new IP (GitHub Actions IPs rotate)
- Naukri detects unusual activity

**Fix:** Try running the workflow once a day consistently (not multiple times). You can also try adding a cookie-based session approach instead of a full login — open an issue if you need help with this.

### Wrong credentials error
Double-check your GitHub Secrets. Make sure there are no leading/trailing spaces in the values.

### GitHub Actions not running on schedule
GitHub may delay or skip scheduled workflows if your repository has had no recent activity. Push a small commit or run it manually to reset. Free GitHub accounts also have a 2,000 minutes/month limit — this workflow uses about 3–5 minutes per run.

---

## Cost

**Free.** GitHub Actions provides 2,000 free minutes/month for private repos. This workflow uses ~3–5 minutes/day = ~90–150 minutes/month — well within the free tier.

---

## Selector reference

If Naukri updates their UI, these are the values to check and update in `NaukriProfileUpdater.java`:

| Constant              | What it targets                        |
|-----------------------|----------------------------------------|
| `SEL_EMAIL_INPUT`     | Email field on login page              |
| `SEL_PASSWORD_INPUT`  | Password field on login page           |
| `SEL_LOGIN_BUTTON`    | Login submit button                    |
| `SEL_HEADLINE_EDIT`   | Pencil/edit icon on headline widget    |
| `SEL_SAVE_BUTTON`     | Save button inside the edit widget     |
