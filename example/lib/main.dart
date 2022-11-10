import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_getnet_pos/flutter_getnet_pos.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _flutterGetnetPosPlugin = FlutterGetnetPos();

  @override
  void initState() {
    super.initState();
    checkService();
  }

  String? serviceStatus;

  String? printerStatus;

  String? mifareStatus;

  String? scannerStatus;

  void impressao() {
    setState(() {
      printerStatus = null;
    });
    FlutterGetnetPos.print(
      [
        "Header is the first line",
        "Content line 1",
        "Content line 2",
      ],
      printBarcode: false, //default is true
    )
        .then((_) => setState(() {
              printerStatus = 'Normal';
            }))
        .catchError((e) => setState(() {
              printerStatus = 'Error: ${e.code} -> ${e.message}';
            }));
  }

  void nfc() {
    setState(() {
      mifareStatus = 'Mantenha o cartão próximo!';
    });
    Future.delayed(const Duration(seconds: 2), () {
      FlutterGetnetPos.getMifareCardSN()
          .then((csn) => setState(() {
                mifareStatus = 'Leitura: $csn';
              }))
          .catchError((e) => setState(() {
                mifareStatus = 'Error: ${e.code} -> ${e.message}';
              }));
    });
  }

  void checkService() async {
    var status = await FlutterGetnetPos.checkService(label: '');
    setState(() {
      serviceStatus = status;
    });
  }

  void scan() {
    setState(() {
      scannerStatus = null;
    });
    FlutterGetnetPos.scan()
        .then((result) => setState(() {
              scannerStatus = 'Leitura: $result';
            }))
        .catchError((e) => setState(() {
              debugPrint(e.toString());
              scannerStatus = 'Error: ${e.code} -> ${e.message}';
            }));
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('FlutterGetnetPosPlugin'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              LabeledValue('Service Status', serviceStatus),
              LabeledValue('Printer:', printerStatus),
              LabeledValue('Mifare:', mifareStatus),
              LabeledValue('Scanner:', scannerStatus),
            ],
          ),
        ),
        floatingActionButton: Padding(
          padding: const EdgeInsets.only(bottom: 8.0),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.end,
            children: <Widget>[
              FloatingActionButton(
                onPressed: nfc,
                child: const Icon(Icons.airplay),
              ),
              const SizedBox(
                width: 10,
              ),
              FloatingActionButton(
                onPressed: scan,
                backgroundColor: Colors.red[900],
                child: const Icon(Icons.camera_alt),
              ),
              const SizedBox(
                width: 10,
              ),
              FloatingActionButton(
                onPressed: impressao,
                backgroundColor: Colors.green[900],
                child: const Icon(Icons.print),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class LabeledValue extends StatelessWidget {
  final String label;
  final String? value;
  const LabeledValue(
    this.label,
    this.value, {
    Key? key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(8.0),
      child: RichText(
        text: TextSpan(
          text: label,
          style: Theme.of(context)
              .textTheme
              .caption!
              .copyWith(fontWeight: FontWeight.bold),
          children: [
            TextSpan(
              text: value ?? ' SEM RESPOSTA ',
              style: Theme.of(context).textTheme.caption,
            ),
          ],
        ),
      ),
    );
  }
}
