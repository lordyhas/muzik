import 'dart:io';
import 'dart:typed_data';

import 'package:equatable/equatable.dart';
import 'package:just_audio/just_audio.dart';
import 'package:just_audio_background/just_audio_background.dart';
import 'package:muzik_audio_player/data/audio_repository/audio_playlist.dart';
import 'package:muzik_audio_player/objectbox.g.dart';
import 'package:on_audio_query/on_audio_query.dart';

class SongInfo extends Equatable {
  Playlist playlist = <SongInfo>[] as Playlist;


  //final SongModel songModel;
  final Uri? artUri;

  final int mid = 0;
  final int id;
  final String title;
  final String displayName;
  final int? size;
  final String? album;
  final String? artist;
  final Duration duration;
  final String filePath;
  final String? fileExtension;
  final bool? isMusic;
  final _map;


  SongInfo({
    required this.id,
    required this.title,
    required this.duration,
    required this.filePath,
    this.album,
    this.artist,
    this.displayName = '',
    this.size,
    this.fileExtension,
    this.isMusic,
    this.artUri,
    Map? map,
  }): _map = map;

  factory SongInfo.fromModel(SongModel songModel) => SongInfo(
    map: songModel.getMap,
    id: songModel.id,
    title: songModel.title,
    filePath: songModel.data,
    displayName: songModel.displayName,
    album: songModel.album,
    artist: songModel.artist,
    duration: Duration(minutes: songModel.duration!),
    //artUri: Uri.parse(_dUri),
  );

  factory SongInfo.fromJson(Map map) => SongInfo(
    //todo: finish implementation
      id: map['id'],
      title: map['title'],
      duration: map['duration'],
      filePath: map['data'],
      album: map['album'],
      artist: map['artist'],
  );

  //String get path => songInfo.data;
  ///

  File byteToFile(Uint8List bytes) {
    //
    //playlist
    return File.fromRawPath(bytes);
    //bytes.toAlbumModel()
    //return File("");

  }

  final String _dUri = "https://upload.wikimedia.org/wikipedia/"
      "commons/e/e8/Music_01754.jpg";

  MediaItem get mediaItem =>
      MediaItem(
        id: "$id",
        title: title,
        displayTitle: displayName,
        album: album,
        artist: artist,
        duration: duration,
        artUri: Uri.parse(_dUri),
        //genre: songInfo.
      );

  Map<String, dynamic> get toMap => _map;
  Map<String, dynamic> toJson() => toMap;

  @override
  // TODO: implement props
  List<Object?> get props => throw UnimplementedError();

}

class MediaSample {
  static int _nextMediaId = 0;
  static final ConcatenatingAudioSource playlist =
  ConcatenatingAudioSource(children: [
    ClippingAudioSource(
      start: const Duration(seconds: 60),
      end: const Duration(seconds: 90),
      child: AudioSource.uri(Uri.parse(
          "https://s3.amazonaws.com/scifri-episodes/scifri20181123-episode.mp3")),
      tag: MediaItem(
        id: '${_nextMediaId++}',
        album: "Science Friday",
        title: "A Salute To Head-Scratching Science (30 seconds)",
        artUri: Uri.parse(
            "https://media.wnyc.org/i/1400/1400/l/80/1/ScienceFriday_WNYCStudios_1400.jpg"),
      ),
    ),
    AudioSource.uri(
      Uri.parse(
          "https://s3.amazonaws.com/scifri-episodes/scifri20181123-episode.mp3"),
      tag: MediaItem(
        id: '${_nextMediaId++}',
        album: "Science Friday",
        title: "A Salute To Head-Scratching Science",
        artUri: Uri.parse(
            "https://media.wnyc.org/i/1400/1400/l/80/1/ScienceFriday_WNYCStudios_1400.jpg"),
      ),
    ),
    AudioSource.uri(
      Uri.parse("https://s3.amazonaws.com/scifri-segments/scifri201711241.mp3"),
      tag: MediaItem(
        id: '${_nextMediaId++}',
        album: "Science Friday",
        title: "From Cat Rheology To Operatic Incompetence",
        artUri: Uri.parse(
            "https://media.wnyc.org/i/1400/1400/l/80/1/ScienceFriday_WNYCStudios_1400.jpg"),
      ),
    ),
    AudioSource.uri(
      Uri.parse("asset:///audio/nature.mp3"),
      tag: MediaItem(
        id: '${_nextMediaId++}',
        album: "Public Domain",
        title: "Nature Sounds",
        artUri: Uri.parse(
            "https://media.wnyc.org/i/1400/1400/l/80/1/ScienceFriday_WNYCStudios_1400.jpg"),
      ),
    ),
  ]);
}
