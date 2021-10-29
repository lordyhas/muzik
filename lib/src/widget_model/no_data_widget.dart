import 'package:flutter/material.dart';
import 'package:flutter/cupertino.dart';

class NoDataWidget extends StatelessWidget {
  final String title;

  const NoDataWidget({required this.title, Key? key}) : super(key: key);

  //const NoDataWidget({this.title});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          const IconButton(
            iconSize: 120,
            onPressed: null,
            icon: Icon(Icons.not_interested),
          ),
          Text(
            title,
            maxLines: 1,
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 16.0,
            ),
          ),
        ],
      ),
    );
  }
}
