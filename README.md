# Devanagari Keyboard (Android IME)

A real, system-wide Hindi/Marathi keyboard for Android — it works inside WhatsApp,
Gmail, Chrome, or any app that accepts text, not just inside a browser page.

## How to get the actual APK (no coding, no Android Studio needed)

1. **Create a free GitHub account** at github.com if you don't have one.
2. **Create a new repository** (e.g. `devanagari-keyboard`), and upload every file
   and folder in this project to it (drag-and-drop works on github.com, or use
   `git push` if you're comfortable with git).
3. As soon as you push, look at the **"Actions"** tab of your repository — a
   workflow called "Build Devanagari Keyboard APK" starts automatically and
   compiles the app in GitHub's own cloud servers (takes ~3-5 minutes).
4. When it finishes (green checkmark), open that workflow run and scroll to
   **"Artifacts"** at the bottom — download **devanagari-keyboard-debug-apk.zip**.
5. Unzip it on your phone (or unzip on your computer and transfer the
   `app-debug.apk` file to your phone via USB/Drive/email).

## Installing on your phone

1. Tap the `app-debug.apk` file. Android will ask to allow installing from this
   source ("install unknown apps") — allow it for that one file/app.
2. Once installed, go to **Settings → System → Languages & input → On-screen
   keyboard → Manage keyboards**, and turn on **"Devanagari Keyboard."**
3. Open any text field, tap the keyboard-switch icon (🌐 or the icon on your
   spacebar) and pick **Devanagari**.

## How to type

Same phonetic layout as the web version:
- **A** → ा/आ, **I** → ि/इ (Shift → ऐ), **E** → ी/ई (Shift → ऍ),
  **U** → ु/उ (Shift → ू/ऊ), **O** → ो/ओ (Shift → ौ/औ)
- **ए key** (right end of the middle row) → े/ए (Shift → ॉ/ऑ).
  Example: देसाई = `d` + `ए` + `s` + `a` + `e`
- **Shift** also gives aspirated consonants: K→ख, T→थ, P→फ, etc.
- **ट-वर्ग** toggle → ट ठ ड ढ ण (stays on until you tap space)
- **़** toggle → adds nukta for the next letter: ड़ ढ़ ज़ फ़
- **" . "** → हलंत (्); **Shift + " . "** → । (danda)

## Shift and ट-वर्ग

- **Shift** (tap once): the key labels change to the shifted letters, for one
  key press. **Double-tap** Shift to lock it (shows "⇧ Lock" in a stronger
  colour); tap once to unlock. After Shift the number row types 1 2 3 ... 0.
- **ट-वर्ग** (tap once): त थ द ध न change to ट ठ ड ढ ण until you press space.
  **Double-tap** to lock it, even across spaces; tap once to unlock.
- **़** changes ड ढ ज फ to their nukta forms for the next letter.

## Updating the keyboard

The APK is now signed with a fixed key (`app/debug.keystore`), so a new build
installs directly over the old one. If Android still says "App not installed"
(only the very first time after switching from an older build), uninstall the
old keyboard app once, then install the new APK.

## Note on this first version

To keep the first build simple and reliable, this native version uses **Shift**
for aspirated sounds (same as our first phonetic web version) rather than the
long-press gesture from the later web version, and it doesn't yet include the
word-suggestion bar. Both could be added in a future version — happy to extend
`DevanagariIME.kt` if you want those back.
