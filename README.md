# Naukri Profile Auto-Updater 🚀

Automatically refreshes your Naukri profile's **"Last Updated"** timestamp every day using Selenium + Java. This pushes your profile to the top of recruiter searches — more visibility, more HR calls!

**How it works:** Naukri sorts profiles by last updated date. A daily automated save (with no actual content changes) updates this timestamp, keeping your profile at the top of search results.

---

## Table of Contents

1. [Project Structure](#project-structure)
2. [Prerequisites](#prerequisites)
3. [Local Setup](#local-setup)
4. [Deployment Options](#deployment-options)
    - [Option A — GitHub Actions (Free, Easy)](#option-a--github-actions-free-easy)
    - [Option B — Oracle Cloud Free VM (Free Forever, Recommended)](#option-b--oracle-cloud-free-vm-free-forever-recommended)
5. [Troubleshooting](#troubleshooting)
6. [Maintenance](#maintenance)

---

## Project Structure

```
naukri-automation/
├── .github/
│   └── workflows/
│       └── naukri-refresh.yml        GitHub Actions cron workflow
├── src/
│   └── main/
│       ├── java/
│       │   └── com/naukri/automation/
│       │       └── NaukriProfileUpdater.java    Main Selenium bot
│       └── resources/
│           └── logback.xml                      Logging config
├── pom.xml                                      Maven build file
├── README.md
└── .gitignore
```

---

## Prerequisites

- Java 17+
- Maven 3.6+
- Google Chrome (for local testing)
- Git
- A Naukri account

---

## Local Setup

### Step 1 — Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/naukri-automation.git
cd naukri-automation
```

### Step 2 — Set your credentials as environment variables

**Windows (Command Prompt):**
```cmd
set NAUKRI_EMAIL=your.email@gmail.com
set NAUKRI_PASSWORD=yourpassword
```

**Windows (PowerShell):**
```powershell
$env:NAUKRI_EMAIL="your.email@gmail.com"
$env:NAUKRI_PASSWORD="yourpassword"
```

**Mac/Linux:**
```bash
export NAUKRI_EMAIL="your.email@gmail.com"
export NAUKRI_PASSWORD="yourpassword"
```

### Step 3 — Build the JAR

```bash
mvn package -DskipTests
```

### Step 4 — Run locally

```bash
java -jar target/naukri-automation-1.0.0.jar
```

### Optional — Watch browser visually (for debugging)

In `NaukriProfileUpdater.java`, comment out headless mode:
```java
// options.addArguments("--headless=new");
```
Rebuild and run — Chrome will open visibly so you can watch the automation in action.

> Remember to uncomment before pushing to GitHub!

---

## Deployment Options

### Option A — GitHub Actions (Free, Easy)

GitHub Actions runs your bot daily for free using GitHub's servers.
**Limitation:** GitHub IPs rotate daily which may trigger OTP from Naukri occasionally.

#### Step 1 — Push to a private GitHub repository

```bash
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/YOUR_USERNAME/naukri-automation.git
git push -u origin main
```

> Use a **private** repository to keep your code and workflow file hidden.

#### Step 2 — Add credentials as GitHub Secrets

1. Go to your repo on GitHub
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret** and add:

| Secret Name | Value |
|---|---|
| `NAUKRI_EMAIL` | your.email@gmail.com |
| `NAUKRI_PASSWORD` | your_naukri_password |

> ⚠️ Never hardcode credentials in your code. Always use secrets.

#### Step 3 — Enable and run the workflow

1. Go to **Actions** tab in your repository
2. Click **Naukri Daily Profile Refresh** in the left sidebar
3. Click **Run workflow** → **Run workflow** to test manually

#### Step 4 — Verify it works

Expand the **"Run Naukri profile updater"** step in the logs. A successful run shows:
```
✅ Profile updated successfully. Timestamp refreshed on Naukri.
```

#### Schedule

The workflow runs daily at **8:00 AM IST** (02:30 UTC) automatically.

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

### Option B — Oracle Cloud Free VM (Free Forever, Recommended)

Oracle Cloud Always Free tier gives you a VM with a **fixed IP address — forever and free**.
A fixed IP means Naukri recognizes your login location and never triggers OTP.

#### Step 1 — Create Oracle Cloud account

1. Go to **cloud.oracle.com** → click **Start for free**
2. Fill in your details and verify your email
3. Add a credit card (required for verification — **you will not be charged**)
4. Choose **Home Region: India South (Hyderabad)**
5. Wait for account activation (5–30 minutes)

#### Step 2 — Create a Free VM instance

1. Go to **☰ Menu** → **Compute** → **Instances** → **Create Instance**
2. Configure:

```
Name:           naukri-bot
Image:          Ubuntu 22.04 LTS  (click Change Image)
Shape:          VM.Standard.E2.1.Micro  (click Change Shape → Micro)
Capacity type:  On-demand  ⚠️ NOT dedicated-host
Public IP:      Assign a public IPv4 address ✅
SSH Keys:       Generate key pair → Download BOTH files → SAVE SAFELY
Storage:        47 GB (default)
```

3. Click **Create** and wait 2–3 minutes

#### Step 3 — Assign a Reserved Public IP (so IP never changes)

1. Go to **Networking** → **IP Management** → **Reserved Public IPs**
2. Click **Reserve Public IP** → give it a name → **Reserve**
3. Go to your instance → **Attached VNICs** → click the VNIC
4. Under **IPv4 Addresses** → click **⋮** → **Edit**
5. Select **Reserved Public IP** → choose the one you created → **Update**

#### Step 4 — Open SSH port in firewall

1. Go to your instance → click the **Subnet** link
2. Click **Security Lists** → click the default security list
3. Click **Add Ingress Rules**:
```
Source CIDR:      0.0.0.0/0
IP Protocol:      TCP
Destination Port: 22
```
4. Save

#### Step 5 — Connect via SSH

**Windows (PowerShell):**
```powershell
ssh -i "C:\path\to\your-private-key.key" ubuntu@YOUR_PUBLIC_IP
```

**Mac/Linux:**
```bash
chmod 400 your-private-key.key
ssh -i "your-private-key.key" ubuntu@YOUR_PUBLIC_IP
```

#### Step 6 — Install dependencies on the VM

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Java 17
sudo apt install -y openjdk-17-jdk
java -version

# Install Maven
sudo apt install -y maven
mvn -version

# Install Chrome
wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
sudo apt install -y ./google-chrome-stable_current_amd64.deb
google-chrome --version

# Install Git
sudo apt install -y git
```

#### Step 7 — Add swap memory (important for 1GB RAM VM)

Chrome needs extra memory. Adding swap prevents crashes:

```bash
sudo fallocate -l 4G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile

# Make permanent across reboots
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Verify
free -h
```

You should see `Swap: 4.0Gi` in the output.

#### Step 8 — Clone your project

```bash
git clone https://github.com/YOUR_USERNAME/naukri-automation.git
cd naukri-automation
```

#### Step 9 — Set credentials permanently

```bash
echo 'export NAUKRI_EMAIL="your.email@gmail.com"' >> ~/.bashrc
echo 'export NAUKRI_PASSWORD="yourpassword"' >> ~/.bashrc
source ~/.bashrc

# Verify
echo $NAUKRI_EMAIL
```

#### Step 10 — Build the JAR

```bash
cd ~/naukri-automation
mvn package -DskipTests
```

#### Step 11 — Test run manually first

```bash
java -jar target/naukri-automation-1.0.0.jar
```

Watch the logs. First run may trigger a one-time OTP from Naukri to verify the new IP.
After that — **Naukri trusts this fixed IP forever. No more OTPs.**

#### Step 12 — Set up daily cron job

```bash
crontab -e
# Choose option 1 (nano) if asked
```

Add this line at the bottom:
```bash
30 2 * * * pkill -f chrome; pkill -f chromedriver; rm -rf /tmp/chrome-* /tmp/org.chromium*; sleep 3; NAUKRI_EMAIL="your.email@gmail.com" NAUKRI_PASSWORD="yourpassword" /usr/bin/java -jar /home/ubuntu/naukri-automation/target/naukri-automation-1.0.0.jar >> /home/ubuntu/naukri-bot.log 2>&1
```

Save: **Ctrl+X → Y → Enter**

Verify:
```bash
crontab -l
```

#### Step 13 — Monitor logs

```bash
# View last 50 lines
tail -50 ~/naukri-bot.log

# Watch live
tail -f ~/naukri-bot.log

# Check if cron ran today
grep CRON /var/log/syslog | grep "$(date '+%b %e')" | tail -10
```

---

## Troubleshooting

### Running manually vs log file

When running the bot manually, output goes to your terminal screen but NOT to the log file.
Use these commands depending on what you want:

**See output on screen only (quick test):**
```bash
NAUKRI_EMAIL="your@email.com" NAUKRI_PASSWORD="yourpassword" java -jar ~/naukri-automation/target/naukri-automation-1.0.0.jar
```

**Write to log file only:**
```bash
NAUKRI_EMAIL="your@email.com" NAUKRI_PASSWORD="yourpassword" java -jar ~/naukri-automation/target/naukri-automation-1.0.0.jar >> ~/naukri-bot.log 2>&1
```

**See output on screen AND write to log file simultaneously (recommended):**
```bash
NAUKRI_EMAIL="your@email.com" NAUKRI_PASSWORD="yourpassword" java -jar ~/naukri-automation/target/naukri-automation-1.0.0.jar 2>&1 | tee -a ~/naukri-bot.log
```

After running, verify the log was written:
```bash
tail -50 ~/naukri-bot.log
```

### Credentials not found
```
NAUKRI_EMAIL or NAUKRI_PASSWORD environment variable is not set
```
**Fix:** Cron jobs don't load `.bashrc`. Set credentials inline in the crontab command as shown in Step 12.

---

### Chrome renderer timeout
```
timeout: Timed out receiving message from renderer
```
**Fix:** Old Chrome processes are consuming all memory. Kill them:
```bash
pkill -f chrome
pkill -f chromedriver
rm -rf /tmp/chrome-* /tmp/org.chromium*
free -h
```
Then run again. Also make sure 4GB swap is active.

---

### Login failed / OTP triggered
Naukri sent an OTP because the IP is new or unrecognized.
- On Oracle Cloud with fixed IP: enter the OTP once → Naukri trusts the IP forever
- On GitHub Actions: IP rotates daily → OTP may trigger occasionally

---

### Could not find Save button / Edit button
Naukri updated their UI and the CSS selectors changed.

1. Open Naukri in Chrome → login → go to profile page
2. Right-click the broken element → **Inspect**
3. Find the correct CSS selector
4. Update the relevant constant in `NaukriProfileUpdater.java`
5. Rebuild and push

---

### Chatbot overlay blocking clicks
```
element click intercepted: chatbot_Overlay show
```
**Fix:** Already handled in the code — overlay is dismissed via JavaScript before any clicks.

---

### ClassNotFoundException on JAR run
```
Could not find or load main class com.naukri.automation.NaukriProfileUpdater
```
**Fix:** Wrong project structure. Ensure the Java file is at exactly:
```
src/main/java/com/naukri/automation/NaukriProfileUpdater.java
```
Then rebuild:
```bash
mvn package -DskipTests
```

---

## Useful Commands

### Check memory and swap
```bash
free -h
swapon --show
```

### Kill all Chrome processes (if bot crashes and leaves zombie processes)
```bash
pkill -f chrome
pkill -f chromedriver
rm -rf /tmp/chrome-* /tmp/org.chromium*
```

### Check if cron ran today
```bash
grep CRON /var/log/syslog | grep "$(date '+%b %e')" | tail -10
```

### View live logs
```bash
tail -f ~/naukri-bot.log
```

### View last 50 lines of log
```bash
tail -50 ~/naukri-bot.log
```

### Full manual run with cleanup + live log output
```bash
pkill -f chrome; pkill -f chromedriver; rm -rf /tmp/chrome-* /tmp/org.chromium*; sleep 3; NAUKRI_EMAIL="your@email.com" NAUKRI_PASSWORD="yourpassword" /usr/bin/java -jar /home/ubuntu/naukri-automation/target/naukri-automation-1.0.0.jar 2>&1 | tee -a ~/naukri-bot.log
```

### Pull latest code changes and rebuild
```bash
cd ~/naukri-automation
git pull origin main
mvn package -DskipTests
```

### Verify crontab
```bash
crontab -l
```

### Check system uptime and load
```bash
uptime
```
---

## Maintenance

### Update project after code changes

On your Oracle VM:
```bash
cd ~/naukri-automation
git pull origin main
mvn package -DskipTests
```

### Check Oracle Cloud billing

Go to **Billing → Costs and Usage** — should always show $0 on Always Free tier.

Set a budget alert just in case:
1. **Billing → Budgets → Create Budget**
2. Amount: **$1**
3. Add your email → save

### Oracle Cloud Free Tier limits (never exceed these)

| Resource | Free Limit |
|---|---|
| VM instances | 2 x VM.Standard.E2.1.Micro |
| Block storage | 200 GB total |
| Reserved Public IPs | 2 |
| Outbound data | 10 TB/month |

### Keeping the VM healthy

Apply security updates monthly:
```bash
sudo apt update && sudo apt upgrade -y
sudo reboot
```

After reboot, reconnect and verify swap is still active:
```bash
free -h
swapon --show
```

---

## How it works (flow)

```
Daily cron / GitHub Actions trigger
    → Launch headless Chrome
    → Navigate to naukri.com homepage
    → Click Login button
    → Fill email and password
    → Submit login
    → Click profile icon in navbar
    → Navigate to profile page
    → Click edit icon on Resume Headline section
    → Click Save (no changes made)
    → Close success popup
    → Click profile icon → Logout
    → Close browser
    → Profile timestamp = "Updated Today" ✅
    → Your profile ranks higher in recruiter search ✅
```

---

## Cost Summary

| Deployment | Cost |
|---|---|
| GitHub Actions | Free (2000 min/month) |
| Oracle Cloud VM | **Free forever** |
| AWS EC2 | Free for 12 months, then ~$8/month |

**Recommended: Oracle Cloud Free VM** — fixed IP, no OTP issues, free forever.

---

## Security Notes

- Never commit credentials to your repository
- Always use environment variables or GitHub Secrets for passwords
- Use a private GitHub repository
- Oracle VM: restrict SSH access to your IP only in security rules
- Change your Naukri password if it was ever accidentally exposed

---

*Built with Selenium 4.x + Java 17 + Maven + GitHub Actions + Oracle Cloud*