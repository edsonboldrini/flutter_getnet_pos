import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_getnet_pos/flutter_getnet_pos_method_channel.dart';

void main() {
  MethodChannelFlutterGetnetPos platform = MethodChannelFlutterGetnetPos();
  const MethodChannel channel = MethodChannel('flutter_getnet_pos');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
