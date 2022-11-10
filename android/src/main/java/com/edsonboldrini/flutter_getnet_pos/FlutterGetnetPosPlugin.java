package com.edsonboldrini.flutter_getnet_pos;

import androidx.annotation.NonNull;

import android.app.Application;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import com.getnet.posdigital.PosDigital;
import com.getnet.posdigital.camera.ICameraCallback;
import com.getnet.posdigital.mifare.IMifareCallback;
import com.getnet.posdigital.printer.AlignMode;
import com.getnet.posdigital.printer.FontFormat;
import com.getnet.posdigital.printer.IPrinterCallback;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * FlutterGetnetPosPlugin
 */
public class FlutterGetnetPosPlugin implements FlutterPlugin, MethodCallHandler {
	/// The MethodChannel that will the communication between Flutter and native Android
	///
	/// This local reference serves to register the plugin with the Flutter Engine and unregister it
	/// when the Flutter Engine is detached from the Activity
	private MethodChannel methodChannel;

	private static final String TAG = "FlutterGetnetPos";
	private static final String NAMESPACE = "flutter_getnet_pos";
	private FlutterPluginBinding pluginBinding;

	private static final Logger LOGGER = Logger.getLogger(FlutterGetnetPosPlugin.class.getName());
	private static final String QR_CODE_PATTERN = "qrCodePattern";
	private static final String BARCODE_PATTERN = "barcodePattern";
	private static final String PRINT_BARCODE = "printBarcode";
	private static final String LIST = "list";
	private Context context;

	@Override
	public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
		Log.d(TAG, "onAttachedToEngine");
		pluginBinding = flutterPluginBinding;
		setup(pluginBinding.getBinaryMessenger(), (Application) pluginBinding.getApplicationContext());
	}

	@Override
	public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
		switch (call.method) {
			case "getPlatformVersion": {
				result.success("Android " + android.os.Build.VERSION.RELEASE);
				break;
			}
			case "check": {
				checkService(result);
				break;
			}
			case "print": {
				print(call, result);
				break;
			}
			case "getMifare": {
				getMifare(result, 3);
				break;
			}
			case "scanner": {
				scanner(result);
				break;
			}
			default: {
				result.notImplemented();
				break;
			}
		}
	}

	@Override
	public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
		methodChannel.setMethodCallHandler(null);
	}

	private void setup(final BinaryMessenger messenger, final Application context) {
		Log.d(TAG, "setup");
		this.context = context;
		methodChannel = new MethodChannel(messenger, NAMESPACE + "/methods");
		methodChannel.setMethodCallHandler(this);

		registerPosDigital(new Callback() {
			@Override
			public void performAction() {
				LOGGER.info("Initialized!");
			}

			@Override
			public void onError(String message) {
				LOGGER.log(Level.SEVERE, message);
			}
		});
	}

	/**
	 * Init the PosDigital Hardware SDK
	 */
	private void ensureInitialized(final Callback callback) {
		try {
			LOGGER.info("Checking service");
			if (!PosDigital.getInstance().isInitiated()) {
				LOGGER.log(Level.SEVERE, "Instance exists, but mainService is null.");
				callback.onError("PosDigital service is not initialized.");
			} else {
				LOGGER.info("PosDigital is registered. Performing action");
				callback.performAction();
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "PosDigital not registered yet, trying now. " + e.getMessage());
			registerPosDigital(callback);
		}
	}

	private void registerPosDigital(final Callback callback) {
		try {
			PosDigital.register(context, new PosDigital.BindCallback() {
				@Override
				public void onError(Exception e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					callback.onError(e.getMessage());
				}

				@Override
				public void onConnected() {
					try {
						callback.performAction();
					} catch (Exception e) {
						callback.onError(e.getMessage());
					}
				}

				@Override
				public void onDisconnected() {
					LOGGER.info("PosDigital service getnet disconnected");
				}
			});
		} catch (Exception e) {
			callback.onError("Failure on service initialization" + e.getMessage());
		}
	}

	/**
	 * Check if the service is initialized
	 *
	 * @param result - result callback
	 */
	private void checkService(Result result) {
		boolean initiated = false;
		try {
			initiated = PosDigital.getInstance().isInitiated();
			PosDigital.getInstance().getBeeper().success();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Failure when checking service: " + e.getMessage());
		}
		result.success(initiated);
	}

	/**
	 * Print
	 *
	 * @param call   - method call
	 * @param result - result callback
	 */
	private void print(final MethodCall call, final Result result) {
		ensureInitialized(new Callback() {
			@Override
			public void performAction() {
				List<String> lines = call.argument(LIST);
				String qrCodePattern = call.argument(QR_CODE_PATTERN);
				String barCodePattern = call.argument(BARCODE_PATTERN);
				boolean printBarcode = call.argument(PRINT_BARCODE);
				if (lines != null && !lines.isEmpty()) {
					try {
						addTextToPrinter(lines, qrCodePattern, barCodePattern, printBarcode);
						callPrintMethod(result);
					} catch (Exception e) {
						result.error("Error on print", e.getMessage(), null);
					}
				} else {
					result.error("Arguments are missed [list, qrCodePattern, barcodePattern]", null, null);
				}
			}

			@Override
			public void onError(String message) {
				result.error("print", message, null);
			}
		});
	}

	/**
	 * Invoke print method and set status of operation on result callback
	 *
	 * @param result - result for handler the status
	 * @throws RemoteException - if the printer is not available
	 */
	private void callPrintMethod(final Result result) throws RemoteException {
		PosDigital.getInstance().getPrinter().print(new IPrinterCallback.Stub() {
			@Override
			public void onSuccess() {
				result.success("Printed.");
			}

			@Override
			public void onError(int i) {
				result.error("Error code: " + i, null, null);
			}
		});
	}

	/**
	 * Add a list of string to the printer buffer
	 *
	 * @param lines - linest to be printed
	 * @throws RemoteException - if printer is not available
	 */
	private void addTextToPrinter(List<String> lines, String qrCodePattern, String barcodePattern, boolean printBarCode) throws RemoteException {
		PosDigital.getInstance().getPrinter().init();
		PosDigital.getInstance().getPrinter().setGray(5);
		PosDigital.getInstance().getPrinter().defineFontFormat(FontFormat.SMALL);
		PosDigital.getInstance().getPrinter().addText(AlignMode.CENTER, lines.remove(0));
		Pattern patternQrCode = Pattern.compile(qrCodePattern, Pattern.MULTILINE);
		Pattern patternBarCode = Pattern.compile(barcodePattern, Pattern.MULTILINE);
		for (String text : lines) {
			for (String line : text.split("\n")) {
				Matcher qrcodeMatcher = patternQrCode.matcher(line);
				Matcher barcodeMather = patternBarCode.matcher(line);
				if (qrcodeMatcher.find()) {
					PosDigital.getInstance().getPrinter().addQrCode(AlignMode.CENTER, 360, line);
				} else if (printBarCode && barcodeMather.find()) {
					PosDigital.getInstance().getPrinter().addText(AlignMode.CENTER, line);
					PosDigital.getInstance().getPrinter().addBarCode(AlignMode.CENTER, line.trim());
					PosDigital.getInstance().getPrinter().addText(AlignMode.CENTER, "");
				} else {
					PosDigital.getInstance().getPrinter().addText(AlignMode.LEFT, line);
				}
			}
		}
	}

	/**
	 * Try to read a next card
	 *
	 * @param result      - result callback
	 * @param remainTries - number of tries
	 */
	private void getMifare(final Result result, final int remainTries) {
		ensureInitialized(new Callback() {
			@Override
			public void performAction() {
				try {
					PosDigital.getInstance().getMifare().searchCard(new IMifareCallback.Stub() {
						@Override
						public void onCard(int i) throws RemoteException {
							result.success(PosDigital.getInstance().getMifare().getCardSerialNo(i));
							PosDigital.getInstance().getMifare().halt();
						}

						@Override
						public void onError(String s) throws RemoteException {
							if (remainTries > 0) {
								getMifare(result, remainTries - 1);
							} else {
								result.error("Error on Mifare", s, null);
							}
						}
					});
				} catch (Exception e) {
					result.error("Error", e.getMessage(), null);
				}
			}

			@Override
			public void onError(String message) {
				result.error("getMifare", message, null);
			}
		});
	}

	/**
	 * Try read some barcode/qrcode using the back camera
	 *
	 * @param result - result callback
	 */
	private void scanner(final Result result) {
		ensureInitialized(new Callback() {
			@Override
			public void performAction() {
				try {
					PosDigital.getInstance().getCamera().readBack(5000, new ICameraCallback.Stub() {
						@Override
						public void onSuccess(String s) throws RemoteException {
							LOGGER.info("Info obtained from scanner: " + s);
							result.success(s);
						}

						@Override
						public void onTimeout() throws RemoteException {
							result.error("Timeout Error", "Timeout expired", null);
						}

						@Override
						public void onCancel() throws RemoteException {
							result.error("Cancelled", "Cancelled by the user", null);
						}

						@Override
						public void onError(String s) throws RemoteException {
							result.error("Error on scanner", s, null);
						}
					});
				} catch (Exception e) {
					result.error("Error", e.getMessage(), null);
				}
			}

			@Override
			public void onError(String message) {
				result.error("scanner", message, null);
			}
		});

	}

	private interface Callback {
		void performAction();

		void onError(String message);
	}
}
