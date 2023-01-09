import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:on_audio_query/on_audio_query.dart';

export 'values/strings.dart';
export 'values/styles.dart';
export 'app_bloc/app_bloc.dart';

//export '';


extension MusicOlny on List<SongModel>{

  List<SongModel> get musicOnly {
    removeWhere((element) =>
    element.fileExtension == "opus" ||
        element.fileExtension == "ogg");
    return this;
  }
}

void setSystemUiOverlayStyle() => SystemChrome.setSystemUIOverlayStyle(SystemUiOverlayStyle(
  statusBarColor: Colors.transparent,
  statusBarIconBrightness: Brightness.light,
  statusBarBrightness: Brightness.dark,
  systemNavigationBarColor: Colors.grey.shade900,
  //systemNavigationBarDividerColor:Theme.of(context).primaryColor,
  systemNavigationBarIconBrightness: Brightness.light,
));