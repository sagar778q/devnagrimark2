package com.example.devakeyboard

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.SystemClock
import android.view.View

class DevanagariIME : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardView: KeyboardView
    private lateinit var keyboard: Keyboard

    private var shift = false
    private var retro = false
    private var nukta = false
    private var lastConsonant = false

    // Double-tap on Shift / ट-वर्ग locks it until it is tapped once more
    private var shiftLock = false
    private var retroLock = false
    private var lastShiftTap = 0L
    private var lastRetroTap = 0L
    private val doubleTapMs = 400L

    data class Forms(
        val base: String,
        val long: String? = null,
        val retro: String? = null,
        val retroLong: String? = null,
        val nukta: String? = null,
        val nuktaLong: String? = null
    )
    data class VowelForm(val matra: String, val standalone: String)

    private val vowels = mapOf(
        'a' to VowelForm("ा", "आ"),
        'i' to VowelForm("ि", "इ"),
        'e' to VowelForm("ी", "ई"),
        'u' to VowelForm("ु", "उ"),
        'o' to VowelForm("ो", "ओ")
    )
    private val vowelsShift = mapOf(
        'a' to VowelForm("", "अ"),
        'i' to VowelForm("ै", "ऐ"),
        'e' to VowelForm("ॅ", "ऍ"),
        'u' to VowelForm("ू", "ऊ"),
        'o' to VowelForm("ौ", "औ")
    )
    private val rVowel = VowelForm("ृ", "ऋ")

    private val forms = mapOf(
        'q' to Forms("ङ"),
        'w' to Forms("ञ"),
        't' to Forms("त", long = "थ", retro = "ट", retroLong = "ठ"),
        'y' to Forms("य"),
        'p' to Forms("प", long = "फ"),
        's' to Forms("स", long = "श"),
        'd' to Forms("द", long = "ध", retro = "ड", retroLong = "ढ", nukta = "ड़", nuktaLong = "ढ़"),
        'f' to Forms("फ", nukta = "फ़"),
        'g' to Forms("ग", long = "घ"),
        'j' to Forms("ज", long = "झ", nukta = "ज़"),
        'k' to Forms("क", long = "ख"),
        'l' to Forms("ल", long = "ळ"),
        'z' to Forms("ज़"),
        'x' to Forms("क्ष", long = "ष"),
        'c' to Forms("च", long = "छ"),
        'v' to Forms("व"),
        'b' to Forms("ब", long = "भ"),
        'n' to Forms("न", retro = "ण"),
        'h' to Forms("ह"),
        'm' to Forms("म"),
        'r' to Forms("र")
    )

    private val digits = mapOf(
        48 to "०", 49 to "१", 50 to "२", 51 to "३", 52 to "४",
        53 to "५", 54 to "६", 55 to "७", 56 to "८", 57 to "९"
    )

    override fun onCreateInputView(): View {
        keyboard = Keyboard(this, R.xml.keyboard_devanagari)
        keyboardView = layoutInflater.inflate(R.layout.input, null) as KeyboardView
        keyboardView.keyboard = keyboard
        keyboardView.setOnKeyboardActionListener(this)
        return keyboardView
    }

    private fun resolveConsonant(c: Char): String {
        val f = forms[c] ?: return c.toString()
        if (nukta && shift && f.nuktaLong != null) return f.nuktaLong
        if (nukta && f.nukta != null) return f.nukta
        if (retro && shift && f.retroLong != null) return f.retroLong
        if (shift && f.long != null) return f.long
        if (retro && f.retro != null) return f.retro
        return f.base
    }

    private fun commit(str: String, newLastConsonant: Boolean) {
        currentInputConnection?.commitText(str, 1)
        lastConsonant = newLastConsonant
    }

    private fun consumeShift() {
        if (!shiftLock) shift = false
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val ic = currentInputConnection ?: return
        val now = SystemClock.uptimeMillis()

        when (primaryCode) {
            Keyboard.KEYCODE_SHIFT -> {
                when {
                    shiftLock -> { shiftLock = false; shift = false }
                    shift && now - lastShiftTap < doubleTapMs -> shiftLock = true
                    else -> shift = !shift
                }
                lastShiftTap = now
            }
            -101 -> {
                when {
                    retroLock -> { retroLock = false; retro = false }
                    retro && now - lastRetroTap < doubleTapMs -> retroLock = true
                    else -> retro = !retro
                }
                lastRetroTap = now
            }
            -102 -> { nukta = !nukta }
            Keyboard.KEYCODE_DELETE -> { ic.deleteSurroundingText(1, 0); lastConsonant = false }
            -103 -> { ic.deleteSurroundingText(10000, 10000); lastConsonant = false }
            -104 -> {
                val out = if (shift) {
                    if (lastConsonant) "ॉ" else "ऑ"
                } else {
                    if (lastConsonant) "े" else "ए"
                }
                commit(out, false)
                consumeShift()
            }
            32 -> { commit(" ", false); if (!retroLock) retro = false }
            10 -> { commit("\n", false); if (!retroLock) retro = false }
            46 -> { commit(if (shift) "।" else "्", !shift); consumeShift() }
            in digits.keys -> {
                commit(if (shift) primaryCode.toChar().toString() else digits[primaryCode]!!, false)
                consumeShift()
            }
            else -> {
                val ch = primaryCode.toChar().lowercaseChar()

                if (vowels.containsKey(ch)) {
                    val vf = if (shift) vowelsShift[ch]!! else vowels[ch]!!
                    commit(if (lastConsonant && vf.matra.isNotEmpty()) vf.matra else vf.standalone, false)
                    consumeShift()
                } else if (ch == 'r' && shift) {
                    commit(if (lastConsonant) rVowel.matra else rVowel.standalone, false)
                    consumeShift()
                } else if (ch == 'h' && shift) {
                    commit("ः", false); consumeShift()
                } else if (ch == 'm' && shift) {
                    commit("ं", false); consumeShift()
                } else {
                    val out = resolveConsonant(ch)
                    commit(out, true)
                    if (nukta) nukta = false
                    consumeShift()
                }
            }
        }

        keyboardView.post { syncKeys() }
    }

    // What each key should show for the current Shift / ट-वर्ग / ़ state
    private fun labelFor(code: Int): CharSequence? {
        if (code == Keyboard.KEYCODE_SHIFT) return if (shiftLock) "⇧ Lock" else "⇧ Shift"
        if (code == 46) return if (shift) "।" else "।/्"
        if (code == -104) return if (shift) "ऑ" else "ए"
        if (code in 48..57) return if (shift) code.toChar().toString() else digits[code]
        if (code !in 97..122) return null
        val ch = code.toChar()
        if (vowels.containsKey(ch)) {
            return (if (shift) vowelsShift[ch]!! else vowels[ch]!!).standalone
        }
        if (shift && ch == 'r') return rVowel.standalone
        if (shift && ch == 'h') return "ः"
        if (shift && ch == 'm') return "ं"
        return resolveConsonant(ch)
    }

    // Updates key highlights and labels after every key press
    private fun syncKeys() {
        keyboard.keys.forEach { k ->
            val code = k.codes[0]
            when (code) {
                Keyboard.KEYCODE_SHIFT -> { k.on = shift; k.pressed = shiftLock }
                -101 -> { k.on = retro; k.pressed = retroLock }
                -102 -> { k.on = nukta; k.pressed = false }
            }
            labelFor(code)?.let { k.label = it }
        }
        keyboardView.invalidateAllKeys()
    }

    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
    override fun onText(text: CharSequence?) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeUp() {}
    override fun swipeDown() {}
}
