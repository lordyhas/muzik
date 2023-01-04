
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:get/get_core/src/get_main.dart';
import 'package:muzik_audio_player/data/audio_repository/audio_song_info.dart';
import 'package:muzik_audio_player/data/values.dart';
import 'package:on_audio_query/on_audio_query.dart';
import 'package:muzik_audio_player/src/music_player_page.dart';

class _DialogContext {
  final BuildContext context;

  _DialogContext(this.context);

  String _fromDuration(Duration duration) {
    int min = duration.inSeconds~/60;
    int sec = ((duration.inSeconds/60) - min).toInt();
    return "$min:$sec";
  }

  void musicInfo({required SongInfo song}) => showDialog(
      context: context,
      builder: (_) {
        SongInfo music = song; //SongInfo.fromModel(song);
        return Material(
          color: Colors.transparent,
          child: Container(
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topCenter ,
                end: Alignment.bottomCenter,
                colors: [
                  Colors.black54,
                  Colors.black54,
                  Colors.black87,
                  Colors.black87,
                  Colors.black87,
                  Colors.black87,
                  Colors.black,
                  Colors.black,
                ],
              ),
            ),
            child: InkWell(
              onTap: Navigator.of(context).pop,
              child: Column(
                children: [
                  /*AppBar(
                    backgroundColor: Colors.transparent,
                    elevation: 0,
                    centerTitle: true,
                    leading: IconButton(
                      icon: const Icon(CupertinoIcons.xmark),
                      onPressed: Navigator.of(context).pop,
                    ),
                    title: const Text('Details'),
                  ),*/
                  Expanded(
                    child: Column(
                      children: [
                        const SizedBox(height: 75,),
                        const Spacer(),
                        Center(
                          child: SizedBox.square(
                            dimension: 200,
                            child: GetImageCoverItem(
                              //fit: BoxFit.fitWidth,
                              futureResource: OnAudioQuery().queryArtwork(
                                music.id,
                                ArtworkType.AUDIO,
                              ),
                            ),
                          ),
                        ),
                        Padding(
                          padding: const EdgeInsets.all(8.0).copyWith(top: 16.0),
                          child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Text(
                                music.title,
                                style: Theme.of(context).textTheme.bodyText1,
                              ),
                              Text(music.artist),
                            ],
                          ),
                        ),
                        const Spacer(),
                        SizedBox(

                          //color: Colors.transparent,
                          child: Column(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              ListTile(
                                title: Text(
                                  "Album",
                                  style: Theme.of(context).textTheme.bodyText2,
                                ),
                                subtitle: Text(
                                  music.album,
                                  style: Theme.of(context).textTheme.bodyText1,
                                ),
                                minVerticalPadding: 0.0,
                                horizontalTitleGap: 0.0,
                                onTap: (){},
                              ),
                              ListTile(
                                title: Text(
                                  "Album artist",
                                  style: Theme.of(context).textTheme.bodyText2,
                                ),
                                subtitle: Text(
                                  music.artist,
                                  style: Theme.of(context).textTheme.bodyText1,
                                ),
                                minVerticalPadding: 0.0,
                                horizontalTitleGap: 0.0,
                                onTap: (){},
                              ),
                              ListTile(
                                title: Text(
                                  "Genre",
                                  style: Theme.of(context).textTheme.bodyText2,
                                ),
                                subtitle: Text(
                                  music.genre ?? "Unknown",
                                  style: Theme.of(context).textTheme.bodyText1,
                                ),
                                minVerticalPadding: 0.0,
                                horizontalTitleGap: 0.0,
                                onTap: (){},
                              ),
                              ListTile(
                                title: Text(
                                  "Track length",
                                  style: Theme.of(context).textTheme.bodyText2,
                                ),
                                subtitle: Text(_fromDuration(music.duration),
                                  style: Theme.of(context).textTheme.bodyText1,
                                ),
                                minVerticalPadding: 0.0,
                                onTap: (){},
                              ),
                              ListTile(
                                title: Text(
                                  "Path",
                                  style: Theme.of(context).textTheme.bodyText2,
                                ),
                                subtitle: Text(
                                  music.filePath
                                    .replaceAll(
                                        "/storage/emulated/0/", "/Internal Storage/")
                                    .replaceAll(music.displayName, "*"),
                                  style: Theme.of(context).textTheme.bodyText1,
                                ),
                                minVerticalPadding: 0.0,
                                onTap: (){},
                              ),

                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(
                    height: 8.0,
                  ),
                ],
              ),
            ),
          ),
        );
      });

  void sliderVolume({
    required String title,
    required int divisions,
    required double min,
    required double max,
    required Stream<double> stream,
    required ValueChanged<double> onChanged,
    String? valueSuffix = '',
  }) => showDialog(
      context: context,
      builder: (context) => AlertDialog(
        backgroundColor: Theme.of(context).backgroundColor.withOpacity(0.9),
        title: Text(title, textAlign: TextAlign.center),
        content: StreamBuilder<double>(
          stream: stream,
          builder: (context, snapshot) => SizedBox(
            height: 100.0,
            child: Column(
              children: [
                Text('${snapshot.data?.toStringAsFixed(1)}$valueSuffix',
                    style: const TextStyle(
                      fontFamily: 'Fixed',
                      fontWeight: FontWeight.bold,
                      fontSize: 24.0,),
                ),
                Slider(
                  //divisions: divisions,
                  min: min,
                  max: max,
                  value: snapshot.data ?? 1.0,
                  onChanged: onChanged,
                  activeColor: Theme.of(context).colorScheme.secondary,
                ),
              ],
            ),
          ),
        ),
      ),
    );

}

class ShowOver {
  static _DialogContext of(BuildContext context) => _DialogContext(context);

  static void musicInfo(context, {required SongInfo song}) =>
      _DialogContext(context).musicInfo(song: song);

  static void sliderVolume(BuildContext context,{
    required String title,
    required int divisions,
    required double min,
    required double max,
    required Stream<double> stream,
    required ValueChanged<double> onChanged,
    String? valueSuffix = '',
  }) => _DialogContext(context).sliderVolume(
    title: title,
    divisions: divisions,
    min: min,
    max: max,
    stream: stream,
    onChanged: onChanged,
  );
}
