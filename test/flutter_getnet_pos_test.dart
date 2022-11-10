import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_getnet_pos/flutter_getnet_pos.dart';
import 'package:flutter_getnet_pos/flutter_getnet_pos_platform_interface.dart';
import 'package:flutter_getnet_pos/flutter_getnet_pos_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterGetnetPosPlatform
    with MockPlatformInterfaceMixin
    implements FlutterGetnetPosPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final FlutterGetnetPosPlatform initialPlatform = FlutterGetnetPosPlatform.instance;

  test('$MethodChannelFlutterGetnetPos is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterGetnetPos>());
  });

  test('getPlatformVersion', () async {
    FlutterGetnetPos flutterGetnetPosPlugin = FlutterGetnetPos();
    MockFlutterGetnetPosPlatform fakePlatform = MockFlutterGetnetPosPlatform();
    FlutterGetnetPosPlatform.instance = fakePlatform;

    expect(await flutterGetnetPosPlugin.getPlatformVersion(), '42');
  });
}
