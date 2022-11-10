import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_getnet_pos_method_channel.dart';

abstract class FlutterGetnetPosPlatform extends PlatformInterface {
  /// Constructs a FlutterGetnetPosPlatform.
  FlutterGetnetPosPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterGetnetPosPlatform _instance = MethodChannelFlutterGetnetPos();

  /// The default instance of [FlutterGetnetPosPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterGetnetPos].
  static FlutterGetnetPosPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterGetnetPosPlatform] when
  /// they register themselves.
  static set instance(FlutterGetnetPosPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
