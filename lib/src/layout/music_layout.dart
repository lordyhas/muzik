library music.layout;
import 'dart:typed_data';
import 'dart:io';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:get/get.dart';

import 'package:muzik_audio_player/src/layout/details_content_screen.dart';
import 'package:muzik_audio_player/src/widget_model/no_data_widget.dart';
import 'package:on_audio_query/on_audio_query.dart';

import 'package:flutter/foundation.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:muzik_audio_player/data/app_bloc/music_player_bloc/player_controller_cubit.dart';
import '../music_player_page.dart';

part 'album_list_screen.dart';
part 'artist_list_screen.dart';
part 'song_list_screen.dart';