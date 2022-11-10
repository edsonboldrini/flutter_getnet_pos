// import 'flutter_getnet_pos_platform_interface.dart';

// class FlutterGetnetPos {
//   Future<String?> getPlatformVersion() {
//     return FlutterGetnetPosPlatform.instance.getPlatformVersion();
//   }
// }

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class FlutterGetnetPos {
  static const String namespace = 'flutter_getnet_pos';

  /// The method channel used to interact with the native platform methods.
  @visibleForTesting
  static MethodChannel methodChannel =
      const MethodChannel('$namespace/methods');

  static Future<String> getPlatformVersion() async =>
      await methodChannel.invokeMethod('getPlatformVersion');

  /// Print a list of strings.
  /// Uses the qrCodePattern to match the qrCode. If matches the qrcode is printed.
  /// Uses the barcodePattern to match the barcode. If matches the barcode is printed.
  static Future<void> print(
    List<String> list, {
    String qrCodePattern = '(\\d{44}\\|.*\$)',
    String barcodePattern = '^\\d{1,}.\$',
    bool printBarcode = true,
  }) async =>
      await methodChannel.invokeMethod('print', {
        'list': list,
        'qrCodePattern': qrCodePattern,
        'barcodePattern': barcodePattern,
        'printBarcode': printBarcode,
      });

  /// Returns the card serial number from Mifare
  static Future<String> getMifareCardSN() async =>
      await methodChannel.invokeMethod('getMifare');

  /// Try scan for QRCode
  static Future<String> scan() async =>
      await methodChannel.invokeMethod('scanner');

  /// Check service status
  static Future<String> checkService({
    String label = 'Service Status',
    String trueMessage = 'On',
    String falseMessage = 'Off',
  }) async {
    var initiated = await methodChannel.invokeMethod('check');
    return "$label: ${initiated ? trueMessage : falseMessage}";
  }
}
