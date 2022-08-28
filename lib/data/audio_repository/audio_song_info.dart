import 'dart:io';
import 'dart:typed_data';

import 'package:equatable/equatable.dart';
import 'package:just_audio/just_audio.dart';
import 'package:just_audio_background/just_audio_background.dart';
import 'package:muzik_audio_player/objectbox.g.dart';
import 'package:objectbox/objectbox.dart';
import 'package:on_audio_query/on_audio_query.dart';

@Entity()
class SongInfo {

  Uri? artUri;
  @Id()
  int uid = 0;
  int id;
  String title;
  String? displayName;
  int? size;
  String album;
  String artist;
  int _duration;
  String filePath;
  String? fileExtension;
  bool? isMusic;

  @Transient()
  final _map;


  SongInfo({
    required this.id,
    required this.title,
    required this.filePath,
    Duration duration = const Duration(),
    this.album = 'Unknown album',
    this.artist = 'Unknown artist',
    this.displayName,
    this.size,
    this.fileExtension,
    this.isMusic,
    this.artUri,
    Map? map,
  }): _duration = duration.inMinutes, _map = map;

  factory SongInfo.fromModel(SongModel songModel) => SongInfo(
    map: songModel.getMap,
    id: songModel.id,
    title: songModel.title,
    filePath: songModel.data,
    displayName: songModel.displayName,
    album: songModel.album ?? 'Unknown album',
    artist: songModel.artist ?? 'Unknown artist',
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

  Duration get duration => Duration(seconds: _duration);
  set duration(Duration duration) => _duration = duration.inSeconds;

  //String get path => songInfo.data;
  /// convert byte as [Uint8List] to file as [File]

  File _byteToFile(Uint8List bytes) {
    return File.fromRawPath(bytes);
  }

  Uri _byteToUri(Uint8List bytes) {
    return Uri.dataFromBytes(bytes);

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

  //@override
  //List<Object?> get props => [id, title, filePath, duration,];

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
