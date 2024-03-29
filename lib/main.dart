import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_localizations/flutter_localizations.dart';

import 'package:just_audio/just_audio.dart';
import 'package:just_audio_background/just_audio_background.dart';
import 'package:audio_session/audio_session.dart';

import 'package:muzik_audio_player/src/home_page.dart';


import 'package:muzik_audio_player/data/values.dart';
import 'package:on_audio_query/on_audio_query.dart';
import 'package:permission_handler/permission_handler.dart';

import 'package:muzik_audio_player/data/database/database_manager.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  //ObjectBoxManager.init();
  await JustAudioBackground.init(
    androidNotificationChannelId: 'com.ryanheise.bg_demo.channel.audio',
    //androidNotificationChannelName: 'Muzik Audio playback',
    androidNotificationOngoing: true,
    //preloadArtwork: true,
    //androidShowNotificationBadge: true,
  ).then((value) => runApp(const MuzikAudioApplication()));
  //runApp(const MuzikAudioApplication());


}

class MuzikAudioApplication extends StatelessWidget {
  const MuzikAudioApplication({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    SystemChrome.setPreferredOrientations(<DeviceOrientation>[
      DeviceOrientation.portraitUp,
      DeviceOrientation.portraitDown
    ]);
    setSystemUiOverlayStyle();

    return MultiBlocProvider(
      providers: [
        BlocProvider<StyleBloc>(
          create: (BuildContext context) => StyleBloc(),
        ),
        BlocProvider<MusicLibraryBloc>(
          create: (BuildContext context) => MusicLibraryBloc(),
        ),
        BlocProvider<PlayerControllerBloc>(
          create: (BuildContext context) => PlayerControllerBloc(),
        ),
        BlocProvider<Languages>(
          create: (BuildContext context) => Languages(),
        ),
        BlocProvider<SettingCubit>(
          create: (BuildContext context) => SettingCubit(),
        ),
        BlocProvider<NavigationController>(
          create: (BuildContext context) => NavigationController(),
        ),

      ],
      child: const MyApp(),
    );
    //const MyApp();
  }
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  AudioPlayer get _player => BlocProvider.of<PlayerControllerBloc>(context).player;
  OnAudioQuery audioQuery = OnAudioQuery();

  Future<void> _init() async {
    final session = await AudioSession.instance;
    await session.configure(const AudioSessionConfiguration.music());
    // Listen to errors during playback.
    _player.playbackEventStream.listen((event) {},
      onError: (Object e, StackTrace stackTrace) {
        debugPrint('A stream error occurred: $e');
      },
    );
    try {
      //await _player.setAudioSource(MediaSample.playlist);
    } catch (e, stackTrace) {
      // Catch load errors: 404, invalid url ...
      debugPrint("Error loading playlist: $e");
      debugPrint(stackTrace.toString());
    }
  }

  @override
  void initState() {
    super.initState();
    requestPermission(); //.then((value) => null);
    _init();
  }

  Future<void> requestPermission() async {
    // Web platform don't support permissions methods.
    if (!kIsWeb) {
      bool permissionStatus = await audioQuery.permissionsStatus();
      if (!permissionStatus) {
        await audioQuery.permissionsRequest();
      }
      //setState(() {});
      //_checkSomePermissions();
    }
  }
  void _checkSomePermissions() async {
    /*if (await Permission.contacts.request().isGranted) {
      // Either the permission was already granted before or the user just granted it.
    }*/

    // You can request multiple permissions at once.
    Map<Permission, PermissionStatus> statuses = await [
      //Permission.location,
      Permission.storage,
      //Permission.camera,
      //Permission.locationAlways,
    ].request();
    debugPrint("storage Permission :"+statuses[Permission.storage].toString());
  }

  @override
  Widget build(BuildContext context) {

    return BlocBuilder<StyleBloc, StyleState>(builder: (_, style) {
      return MaterialApp(
        debugShowCheckedModeBanner: false,
        title: 'Muzik Audio Player',
        theme: style.theme,
        localizationsDelegates: const [
          AppLocalizations.delegate, // Add this line
          GlobalMaterialLocalizations.delegate,
          GlobalWidgetsLocalizations.delegate,
          GlobalCupertinoLocalizations.delegate,
        ],
        supportedLocales: const [
          Locale('en', ''), // English, no country code
          Locale('fr', ''), // Spanish, no country code
        ],
        home: BlocBuilder<PlayerControllerBloc, PlayerControllerState>(
            builder: (_, player) {
          //_init();
          return const HomePage();
        }),
      );
    });
  }
}


class _MaterialAppProvider extends StatelessWidget {
  final List<DeviceOrientation> deviceOrientations;

  const _MaterialAppProvider({
    this.deviceOrientations = const <DeviceOrientation>[],
    Key? key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    SystemChrome.setPreferredOrientations(<DeviceOrientation>[
      DeviceOrientation.portraitUp,
      DeviceOrientation.portraitDown
    ]);

    return MultiBlocProvider(
      providers: [
        BlocProvider<MusicLibraryBloc>(
          create: (BuildContext context) => MusicLibraryBloc(),
        ),
        BlocProvider<PlayerControllerBloc>(
          create: (BuildContext context) => PlayerControllerBloc(),
        ),
        BlocProvider<Languages>(
          create: (BuildContext context) => Languages(),
        ),
        BlocProvider<StyleBloc>(
          create: (BuildContext context) => StyleBloc(),
        ),
      ],
      child: const MaterialApp(

      ),
    );
    //const MyApp();
  }
}
