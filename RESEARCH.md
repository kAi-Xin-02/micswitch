# MicSwitch - Project Archive

## Why This Project Failed

Android's audio system gives exclusive microphone access to apps. Once an app starts recording, it holds the mic until it voluntarily releases it. No amount of permissions manipulation can change an active audio session.

## What Was Tried

1. **Permission Revocation** - Doesn't affect active sessions
2. **AppOps Manipulation** - Same issue
3. **Force Stop** - Works but disconnects the call (defeats purpose)
4. **Audio Policy Commands** - Limited access even with root
5. **AudioFlinger Manipulation** - Device-specific, unreliable

## Lessons Learned

- Research Android limitations before starting
- Some features are impossible without OS modification
- Shizuku ≠ Root (it's just ADB shell)
- Audio/Camera are exclusively locked resources

## Code Structure

- `core/` - Business logic
- `service/` - Floating button service
- `ui/` - MainActivity
- Uses Root (libsu) and Shizuku APIs
- Material Design UI

## If Someone Wants to Continue

You'd need to:
1. Create a custom Android ROM
2. OR use Xposed/LSposed modules to hook AudioRecord
3. OR find device-specific audio HAL exploits

Regular app development cannot solve this problem.

## Contact

star.light0x.2@gmail.com
