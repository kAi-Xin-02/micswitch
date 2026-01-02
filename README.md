## MicSwitch - Microphone Switcher for Android

**⚠️ PROJECT DISCONTINUED**

### What it was supposed to do:
Switch microphone between two apps (Discord/WhatsApp) without disconnecting

### Why it failed:
- Android's audio architecture doesn't allow mic sharing
- Apps hold exclusive mic access during calls
- Even with Root/Shizuku, can't transfer active mic session

### What it CAN do:
- Detect root/Shizuku
- Show floating button ( js click and it will change mic permission ) 
- Toggle mic permissions (only affects new sessions)

### Requirements:
- Android 8.0+
- Root or Shizuku
- Overlay permission

### Known Issues:
- Mic doesn't actually switch during active calls 
- Force stopping apps disconnects voice calls ( fixed ) 
- Permission changes don't affect ongoing sessions

### Technical Limitations:
Android's AudioRecord API gives exclusive mic access to first app that requests it.
No way to override this without modifying Android OS itself.

**Learning project - Not functional for intended purpose**
