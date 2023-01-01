
import 'package:animated_text_kit/animated_text_kit.dart';
import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

import 'package:flutter/services.dart';

import 'package:package_info_plus/package_info_plus.dart';
import 'package:url_launcher/url_launcher.dart';

class AboutWidget extends StatefulWidget{
  const AboutWidget({Key? key, this.title}) : super(key: key);
  final String? title;
  @override
  State<StatefulWidget> createState() {
    //throw UnimplementedError();
    return AboutState();
  }

  static MaterialPageRoute route() => MaterialPageRoute(
      builder: (BuildContext context) =>  const AboutWidget());
}

class AboutState extends State<AboutWidget>{
  final TextStyle primaryTextStyle20 =  const TextStyle(color: Colors.white, fontSize: 20);
  var timeUpdate =  DateTime.now();
  var textWhiteColor = Colors.white;
  String appVersion = "searching...";
  TextStyle textSettingsStyle() => TextStyle(color: Colors.blue[600]);

  void _defaultOnTap(){

    ScaffoldMessenger.of(context)
      ..hideCurrentSnackBar()
      ..showSnackBar(const SnackBar(
          behavior: SnackBarBehavior.floating,
          content: Text('En développment | Soon :) ')
      ));

  }

  @override
  Widget build(BuildContext context) {


    return Scaffold(

      appBar: AppBar(
        centerTitle: true,
        title: const Text("About Us & App"),
      ),
      body: contentAbout(context),

    );
  }
  Widget contentAbout(context){
    return ListView(
      physics: const BouncingScrollPhysics(),
      padding: const EdgeInsets.only(left: 5.0, right: 5.0,),
      children: <Widget>[
        const SizedBox(height: 8.0,),
        Card(
          ///color: background2,
          child: Column(
            children: <Widget>[
              ListTile(
                leading: const CircleAvatar(
                  backgroundColor: Colors.lightBlue,
                  backgroundImage: AssetImage("assets/app_icon.png"),
                ),
                title: AnimatedTextKit(
                  animatedTexts: [
                    TypewriterAnimatedText(
                      'Music P',
                      speed: const Duration(milliseconds: 150),
                      curve: Curves.bounceIn,
                      textStyle:  TextStyle(color: textWhiteColor, fontSize: 24),
                    ),
                    //TyperAnimatedText(text)
                    TyperAnimatedText(
                      'Muzik Player',
                      speed:  const Duration(milliseconds: 150),
                      curve: Curves.easeInCirc,
                      textStyle:  TextStyle(color: textWhiteColor, fontSize: 24),
                    ),


                  ],
                  //isRepeatingAnimation: true,
                  //repeatForever: true,
                  displayFullTextOnTap: true,
                  stopPauseOnTap: false,
                  totalRepeatCount: 1,
                ), /*Text(
                  "M'ziki Audio Player",
                  style: TextStyle(color: textWhiteColor, fontSize: 24),),*/
                subtitle: const Text("@lordyhas",style:  TextStyle(color: Colors.white54),),

              ),
              ListTile(
                leading: const Icon(Icons.info),
                title: Text("Version",style: primaryTextStyle20,),
                subtitle: FutureBuilder<PackageInfo>(
                  future: PackageInfo.fromPlatform(),
                  builder: (context,snapshot){
                    if(snapshot.hasData){
                      appVersion = snapshot.data!.version;

                      return Text("$appVersion (non-stable)",
                      style: TextStyle(
                          color: Theme.of(context).primaryColor),
                    );}
                    return Text(appVersion,
                      style: TextStyle(
                          color: Theme.of(context).primaryColor),
                    );
                  },

                ),

              ),
              ListTile(
                leading: const Icon(Icons.update),
                title: Text("Dernière mise à jour",style: primaryTextStyle20,),
                subtitle: Text(
                    "${timeUpdate.subtract(const Duration(days: 30, hours: 1))}",
                    style:  TextStyle(color: Theme.of(context).primaryColor)
                ),

              ),
              ListTile(
                leading: const Icon(Icons.sync),
                title: Text("Vérifier la mise à jour",style: primaryTextStyle20,),
                onTap: _defaultOnTap,
              ),
              ListTile(
                leading: const Icon(Icons.turned_in_not),
                title: Text("Licences",style: primaryTextStyle20,),
                onTap: () async {
                  PackageInfo packageInfo = await PackageInfo.fromPlatform();
                  return showAboutDialog(
                    context: context,
                    applicationName: packageInfo.appName,
                    applicationVersion: packageInfo.version,
                    applicationIcon: const CircleAvatar(
                      backgroundColor: Colors.lightBlue,
                      backgroundImage: AssetImage("assets/app_icon.png"),
                    ),

                  );
                }
              ),
            ],
          ),
        ),

        Card(
          //color: background2,
          child: Column(
            children: <Widget>[

              ListTile(
                  title: Text("Author",style: textSettingsStyle(),),
              ),
              ListTile(
                leading: const Icon(FontAwesomeIcons.user),
                title: Text("Hassan K.",style: primaryTextStyle20,),
                subtitle: Text("@lordyhas",style:  TextStyle(color: Theme.of(context).primaryColor)),
                // haspro@gmail.com
                onTap: () async {
                  final Uri url = Uri.parse("https://linktr.ee/hassankajila");
                  if (!await launchUrl(url)) {
                    throw 'Could not launch $url';
                  }
                },
              ),
              ListTile(
                leading: const Icon(FontAwesomeIcons.googlePlay),
                title: Text("Play Store",style: primaryTextStyle20,),
                onTap: () async {
                  PackageInfo packageInfo = await PackageInfo.fromPlatform();
                  String appPackageName = packageInfo.packageName;
                  final link = "https://play.google.com/store/apps/details?id=" + appPackageName;
                  final Uri url = Uri.parse(link);
                  if (!await launchUrl(url)) {
                    throw 'Could not launch $url';
                  }
                },
              ),
            ],
          ),
        ),
        Card(
          //color: background2,
          child: Column(
            children: <Widget>[

              ListTile(
                title: Text("Company",style: textSettingsStyle(),),
              ),
              ListTile(
                leading: const Icon(Icons.business),
                title: Text("KDynamic Lab.",style: primaryTextStyle20,),
                subtitle: Text("Mobile App Developers ",style:  TextStyle(color: Theme.of(context).primaryColor)),
                onTap: _defaultOnTap,

              ),
              ListTile(
                leading: const Icon(Icons.location_on),
                title: Text("Address",style: primaryTextStyle20,),
                subtitle: Text("None ",style:  TextStyle(color: Theme.of(context).primaryColor)),
              ),

            ],
          ),
        ),
      ],
    );
  }
}