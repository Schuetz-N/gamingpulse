# Adding Sources to GamingPulse

## Overview

GamingPulse uses a plugin-based source architecture. Each content source is defined in a YAML config file under `sources/`. Adding a new source requires two steps: creating the config file and wiring it into n8n.

## Step 1: Create a Source Config

Create a new YAML file in `sources/`:

```yaml
# sources/arstechnica.yml
name: Ars Technica Gaming
type: rss
url: https://feeds.arstechnica.com/arstechnica/gaming
category: gaming          # gaming | hardware | proton
enabled: true
poll_interval_minutes: 30
filters:
  exclude_keywords:
    - sponsored
    - deals
  min_word_count: 100
```

### Supported Source Types

| Type | URL Format | Notes |
|---|---|---|
| `rss` | Standard RSS/Atom feed URL | Most news sites |
| `youtube` | `https://www.youtube.com/feeds/videos.xml?channel_id=CHANNEL_ID` | Use channel ID, not name |
| `github_releases` | `https://github.com/owner/repo/releases.atom` | Track new releases |
| `scraper` | Any URL | Requires custom Spring Boot parser |

### Categories

| Category | Hashtag | Use for |
|---|---|---|
| `gaming` | #gaming | Game news, releases, reviews |
| `hardware` | #hardware | GPUs, CPUs, peripherals, benchmarks |
| `proton` | #proton | Linux gaming, Wine, Proton, compatibility |

## Step 2: Wire into n8n

1. Open n8n at http://localhost:5678
2. Open the GamingPulse workflow
3. Add a new **RSS Read** node with the feed URL
4. Connect **Schedule Trigger** → new RSS Read node
5. Connect new RSS Read node → **Merge** node
6. In the **Code in JavaScript** (dedup) node, add the domain to `sourceMap` and `getDomain`

### Example: Adding Ars Technica

In the dedup Code node, add to `sourceMap`:

```javascript
'arstechnica.com': { source: 'Ars Technica', category: 'gaming' },
```

And add to `getDomain`:

```javascript
if (url.includes('arstechnica.com')) return 'arstechnica.com';
```

## Finding RSS Feed URLs

Most news sites have RSS feeds. Common patterns:

- `/rss` or `/feed` appended to the domain
- `/rss.xml` or `/feed.xml`
- Look for the RSS icon on the site
- Check the page source for `<link rel="alternate" type="application/rss+xml">`

### YouTube Channels

Every YouTube channel has an RSS feed:

```
https://www.youtube.com/feeds/videos.xml?channel_id=CHANNEL_ID
```

To find the channel ID: go to the channel page → View Source → search for `channelId`.

### GitHub Releases

Every GitHub repository has an Atom feed for releases:

```
https://github.com/owner/repo/releases.atom
```
