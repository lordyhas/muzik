import 'package:bloc/bloc.dart';
import 'package:flutter/material.dart';
import 'package:meta/meta.dart';

enum ThemeModeState{classic, cyan, orange, special, cool, random}

class StyleState{
  final ThemeModeState themeMode;
  final ThemeData theme;

  const StyleState._({
    required this.themeMode,
    required this.theme,
  });


  StyleState.initial() :
        this._(themeMode: ThemeModeState.classic, theme: Themes.themePurple);

  factory StyleState.themeMode(ThemeModeState themeMode) {
    switch(themeMode){
      case ThemeModeState.classic:
        return StyleState._(themeMode: themeMode, theme: Themes.themePurple);
      case ThemeModeState.orange:
        return StyleState._(themeMode: themeMode, theme: Themes.themeOrange);
      case ThemeModeState.cyan:
        return StyleState._(themeMode: themeMode, theme: Themes.themeBlue);
      case ThemeModeState.special:
        return StyleState._(themeMode: themeMode, theme: Themes.themeSpecial);
      case ThemeModeState.cool:
        return StyleState._(themeMode: themeMode, theme: Themes.themeBlue);
      case ThemeModeState.random:
        return StyleState._(themeMode: themeMode, theme: Themes.themeBlue);

    }


  }


}
class StyleBloc extends Cubit<StyleState> {
  StyleBloc() : super(StyleState.initial());


  ThemeModeState get themeMode => state.themeMode;


  //void changeTheme(ThemeData theme) => emit(theme);
  void switchThemeTo(ThemeModeState state){
    emit(StyleState.themeMode(state));

  }
}

class Themes {

  static final ThemeData _generalTheme = ThemeData(
    brightness: Brightness.dark,
    //primarySwatch: Colors.blue,
    //fontFamily: "ubuntu",
    textTheme: const TextTheme(
      bodyText1: TextStyle(
        fontSize: 20,
        color: Colors.white,
      ),
    ),
    appBarTheme: AppBarTheme(
      titleTextStyle: const TextStyle(color: Colors.white, fontSize: 18),
      backgroundColor: Colors.grey[800],
      iconTheme: const IconThemeData(
        color: Colors.white,
      ),
    ),
    colorScheme: ColorScheme.fromSwatch().copyWith(
      brightness: Brightness.dark,
    ),
    backgroundColor: Colors.grey[900],

    primaryColorLight: Colors.white,
    iconTheme: const IconThemeData(color: Colors.white),
    bottomSheetTheme: const BottomSheetThemeData(
      backgroundColor: Colors.transparent,
    ),
  );

  static final ThemeData themePurple = _generalTheme.copyWith(
    primaryColor: Colors.deepPurple,
    primaryColorDark: Colors.deepPurple.shade600,
    colorScheme: const ColorScheme.dark().copyWith(
      secondary: Colors.deepPurpleAccent,
    ),
    useMaterial3: true,
  );

  static final ThemeData themeOrange = ThemeData(
      brightness: Brightness.dark,
      //primarySwatch: Colors.blue,
      //fontFamily: "ubuntu",
      textTheme: const TextTheme(
        bodyText1: TextStyle(
          color: Colors.white,
        ),
      ),
      appBarTheme: AppBarTheme(
        titleTextStyle: const TextStyle(color: Colors.white, fontSize: 18),
        backgroundColor: Colors.grey[800],
        iconTheme: const IconThemeData(
          color: Colors.white,
        ),
      ),
      colorScheme: ColorScheme.fromSwatch().copyWith(
        secondary: Colors.deepOrangeAccent,
        brightness: Brightness.dark,
      ),
      backgroundColor: Colors.grey[900],
      primaryColor: Colors.deepOrange,
      primaryColorDark: Colors.deepOrange.shade600,
      primaryColorLight: Colors.white,
      iconTheme: const IconThemeData(color: Colors.white),
      bottomSheetTheme: const BottomSheetThemeData(
        backgroundColor: Colors.transparent,
      )
  );


  static final ThemeData themeBlue = ThemeData(
      brightness: Brightness.dark,
      //primarySwatch: Colors.blue,
      //fontFamily: "ubuntu",
      textTheme: const TextTheme(
        bodyText1: TextStyle(
          color: Colors.white,
        ),
      ),
      appBarTheme: AppBarTheme(
          titleTextStyle: const TextStyle(color: Colors.white, fontSize: 18),
          backgroundColor: Colors.grey[800],
          iconTheme: const IconThemeData(
            color: Colors.white,
          ),
      ),
      colorScheme: ColorScheme.fromSwatch().copyWith(
        secondary: Colors.cyanAccent,
        brightness: Brightness.dark,
      ),
      backgroundColor: Colors.grey[900],
      primaryColor: Colors.cyan,
      primaryColorDark: Colors.cyan.shade600,
      primaryColorLight: Colors.white,
      iconTheme: const IconThemeData(color: Colors.white),
      bottomSheetTheme: const BottomSheetThemeData(
        backgroundColor: Colors.transparent,
      )
  );


  static final ThemeData themeSpecial = ThemeData(
      brightness: Brightness.dark,
      //primarySwatch: Colors.blue,
      //fontFamily: "ubuntu",
      textTheme: const TextTheme(
        bodyText1: TextStyle(
          fontSize: 22,
          color: Colors.white,
        ),
      ),
      appBarTheme: AppBarTheme(
        titleTextStyle: const TextStyle(color: Colors.white, fontSize: 18),
        backgroundColor: Colors.grey[800],
        iconTheme: const IconThemeData(
          color: Colors.white,
        ),
      ),
      colorScheme: ColorScheme.fromSwatch().copyWith(
        secondary: Colors.deepPurple.shade600,
        brightness: Brightness.dark,
      ),
      backgroundColor: Colors.grey[900],
      primaryColor: Colors.deepPurpleAccent,
      primaryColorDark: Colors.deepPurple,
      primaryColorLight: Colors.white,
      iconTheme: const IconThemeData(color: Colors.white),
      bottomSheetTheme: const BottomSheetThemeData(
        backgroundColor: Colors.transparent,
      ),
  );
}


class HexColor extends Color {
  HexColor(final String hexColor) : super(_getColorFromHex(hexColor));

  static int _getColorFromHex(String hexColor) {
    hexColor = hexColor.toUpperCase().replaceAll('#', '');
    if (hexColor.length == 6) {
      hexColor = 'FF' + hexColor;
    }
    return int.parse(hexColor, radix: 16);
  }
}
