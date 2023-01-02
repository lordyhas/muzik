import 'dart:io';
import 'dart:typed_data';

import 'package:equatable/equatable.dart';
import 'package:just_audio/just_audio.dart';
import 'package:just_audio_background/just_audio_background.dart';
//import 'package:muzik_audio_player/objectbox.g.dart';
//import 'package:objectbox/objectbox.dart';
import 'package:on_audio_query/on_audio_query.dart';

//@Entity()
class SongInfo {

  Uri? artUri;
  //@Id()
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

  //@Transient()
  //final _map;



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
  }): _duration = duration.inMinutes;

  factory SongInfo.fromModel(SongModel songModel) => SongInfo(
    //map: songModel.getMap,
    id: songModel.id,
    title: songModel.title,
    filePath: songModel.data,
    displayName: songModel.displayName,
    album: songModel.album ?? 'Unknown album',
    artist: songModel.artist ?? 'Unknown artist',
    duration: Duration(minutes: songModel.duration!),
    //artUri: Uri.parse(_dUri),
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
  //Map<String, dynamic> toMap() => toJson();


  //@override
  //List<Object?> get props => [id, title, filePath, duration,];

  MediaItem get mediaItem => MediaItem(
    id: "$id",
    title: title,
    displayTitle: displayName,
    album: album,
    artist: artist,
    duration: duration,
    artUri: Uri.parse(MediaSample.wikiUri),
    //genre: songInfo.
  );


  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is SongInfo &&
          runtimeType == other.runtimeType &&
          artUri == other.artUri &&
          id == other.id &&
          title == other.title &&
          displayName == other.displayName &&
          size == other.size &&
          album == other.album &&
          artist == other.artist &&
          _duration == other._duration &&
          filePath == other.filePath &&
          fileExtension == other.fileExtension &&
          isMusic == other.isMusic);

  @override
  int get hashCode =>
      artUri.hashCode ^
      id.hashCode ^
      title.hashCode ^
      displayName.hashCode ^
      size.hashCode ^
      album.hashCode ^
      artist.hashCode ^
      _duration.hashCode ^
      filePath.hashCode ^
      fileExtension.hashCode ^
      isMusic.hashCode;

  @override
  String toString() {
    return 'SongInfo('
        ' artUri: $artUri,'
        ' id: $id,'
        ' title: $title,'
        ' displayName: $displayName,'
        ' size: $size,'
        ' album: $album,'
        ' artist: $artist,'
        ' _duration: $_duration,'
        ' filePath: $filePath,'
        ' fileExtension: $fileExtension,'
        ' isMusic: $isMusic,'
        ')';
  }

  SongInfo copyWith({
    Uri? artUri,
    int? id,
    String? title,
    String? displayName,
    int? size,
    String? album,
    String? artist,
    Duration? duration,
    String? filePath,
    String? fileExtension,
    bool? isMusic,
  }) {
    return SongInfo(
      artUri: artUri ?? this.artUri,
      id: id ?? this.id,
      title: title ?? this.title,
      displayName: displayName ?? this.displayName,
      size: size ?? this.size,
      album: album ?? this.album,
      artist: artist ?? this.artist,
      duration: duration ?? Duration(minutes: _duration),
      filePath: filePath ?? this.filePath,
      fileExtension: fileExtension ?? this.fileExtension,
      isMusic: isMusic ?? this.isMusic,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'artUri': artUri,
      'id': id,
      'title': title,
      'displayName': displayName,
      'size': size,
      'album': album,
      'artist': artist,
      '_duration': _duration,
      'filePath': filePath,
      'fileExtension': fileExtension,
      'isMusic': isMusic,
    };
  }

  factory SongInfo.fromMap(Map<String, dynamic> map) {
    return SongInfo(
      artUri: map['artUri'] as Uri,
      id: map['id'] as int,
      title: map['title'] as String,
      displayName: map['displayName'] as String,
      size: map['size'] as int,
      album: map['album'] as String,
      artist: map['artist'] as String,
      duration: Duration(minutes: map['_duration'] as int),
      filePath: map['filePath'] as String,
      fileExtension: map['fileExtension'] as String,
      isMusic: map['isMusic'] as bool,
    );
  }

  Map<String, dynamic> toJson() => toMap();
  factory SongInfo.fromJson(Map<String, dynamic> map) => SongInfo.fromMap(map);

//</editor-fold>
}

class MediaSample {
  static const String wikiUri = "https://upload.wikimedia.org/wikipedia/"
      "commons/e/e8/Music_01754.jpg";
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
