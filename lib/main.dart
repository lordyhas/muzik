import 'package:audio_session/audio_session.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:just_audio/just_audio.dart';
import 'package:just_audio_background/just_audio_background.dart';
import 'package:muzik_audio_player/data/values.dart';
import 'package:muzik_audio_player/src/home_page.dart';
import 'package:on_audio_query/on_audio_query.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  await SystemChrome.setPreferredOrientations(<DeviceOrientation>[
    DeviceOrientation.portraitUp,
    DeviceOrientation.portraitDown,
  ]);

  await JustAudioBackground.init(
    androidNotificationChannelId: 'com.ryanheise.bg_demo.channel.audio',
    androidNotificationOngoing: true,
  );

  runApp(const MuzikAudioApplication());
}

class MuzikAudioApplication extends StatelessWidget {
  const MuzikAudioApplication({super.key});

  @override
  Widget build(BuildContext context) {
    setSystemUiOverlayStyle();

    return MultiBlocProvider(
      providers: <BlocProvider<dynamic>>[
        BlocProvider<StyleBloc>(create: (_) => StyleBloc()),
        BlocProvider<MusicLibraryBloc>(create: (_) => MusicLibraryBloc()),
        BlocProvider<PlayerControllerBloc>(
          create: (_) => PlayerControllerBloc(),
        ),
        BlocProvider<Languages>(create: (_) => Languages()),
        BlocProvider<SettingCubit>(create: (_) => SettingCubit()),
        BlocProvider<NavigationController>(
          create: (_) => NavigationController(),
        ),
      ],
      child: const MyApp(),
    );
  }
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  AudioPlayer get _player => context.read<PlayerControllerBloc>().player;
  final OnAudioQuery _audioQuery = OnAudioQuery();

  @override
  void initState() {
    super.initState();
    _requestPermission();
    _initAudioSession();
  }

  Future<void> _initAudioSession() async {
    final AudioSession session = await AudioSession.instance;
    await session.configure(const AudioSessionConfiguration.music());

    _player.playbackEventStream.listen(
      (_) {},
      onError: (Object error, StackTrace stackTrace) {
        debugPrint('A stream error occurred: $error');
      },
    );
  }

  Future<void> _requestPermission() async {
    if (!kIsWeb) {
      final bool permissionStatus = await _audioQuery.permissionsStatus();
      if (!permissionStatus) {
        await _audioQuery.permissionsRequest();
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<StyleBloc, StyleState>(
      builder: (_, StyleState style) {
        return MaterialApp(
          debugShowCheckedModeBanner: false,
          title: 'Muzik Audio Player',
          theme: style.theme,
          localizationsDelegates: const <LocalizationsDelegate<dynamic>>[
            AppLocalizations.delegate,
            GlobalMaterialLocalizations.delegate,
            GlobalWidgetsLocalizations.delegate,
            GlobalCupertinoLocalizations.delegate,
          ],
          supportedLocales: const <Locale>[
            Locale('en'),
            Locale('fr'),
          ],
          home: const HomePage(),
        );
      },
    );
  }
}
