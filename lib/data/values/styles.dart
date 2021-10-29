import 'package:bloc/bloc.dart';
import 'package:flutter/material.dart';
import 'package:meta/meta.dart';


class Styles extends Cubit<ThemeData> {
  Styles() : super(_theme1);

  static final ThemeData _theme1 = ThemeData.dark().copyWith(
    appBarTheme:  AppBarTheme(
      backgroundColor: Colors.grey.shade800,
    ),
    primaryColor: Colors.deepPurpleAccent,
    primaryColorLight: Colors.purpleAccent,
    primaryColorDark: Colors.deepPurple

  );
}
