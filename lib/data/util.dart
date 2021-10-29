import 'package:flutter/material.dart';

class Log {
  static void out(String tag, String message){
    debugPrint('# $tag : $message');
  }

  static void error(Exception e, String message){
    debugPrint('#ERROR ${e.toString()} : $message');
  }

}