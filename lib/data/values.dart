import 'package:on_audio_query/on_audio_query.dart';

export 'values/strings.dart';
export 'values/styles.dart';
export 'package:get/get.dart';
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