import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_getnet_pos_platform_interface.dart';

/// An implementation of [FlutterGetnetPosPlatform] that uses method channels.
class MethodChannelFlutterGetnetPos extends FlutterGetnetPosPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_getnet_pos');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
