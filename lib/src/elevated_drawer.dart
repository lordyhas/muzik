part of 'home_page.dart';

class ElevatedDrawer extends StatelessWidget {
  final void Function()? onTapItem;
  final Color? color;
  const ElevatedDrawer({ this.color,this.onTapItem, Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Material(
      color: color ?? Colors.grey.shade700.withOpacity(0.7),
      child: InkWell(
        onTap: onTapItem,
        child: Column(
          children: [
            ListTile(
              title: const Text("Favorite",
                style: TextStyle(fontSize: 20,fontWeight: FontWeight.bold),
              ),
              leading: const ImageIcon(AssetImage(Res.fav_song_r), size: 30),
              trailing: const Icon(CupertinoIcons.chevron_right),
              onTap: (){
                onTapItem!();
              },
            ),
            ListTile(
              title: const Text("Playlist",
                style: TextStyle(fontSize: 20,fontWeight: FontWeight.bold),
              ),
              leading: const Icon(CupertinoIcons.music_note_list),
              trailing: const Icon(CupertinoIcons.chevron_right),
              onTap: (){
                onTapItem!();
              },
            ),
            ListTile(
              title: const Text("Songs",
                style: TextStyle(fontSize: 20,fontWeight: FontWeight.bold),
              ),
              leading: const Icon(CupertinoIcons.music_note),
              trailing: const Icon(CupertinoIcons.chevron_right),
              onTap: (){
                onTapItem!();
              },
            ),
            ListTile(
              title: const Text("Album",
                style: TextStyle(fontSize: 20,fontWeight: FontWeight.bold),
              ),
              leading: const Icon(CupertinoIcons.music_albums),
              trailing: const Icon(CupertinoIcons.chevron_right),
              onTap: (){
                onTapItem!();
              },
            ),
            ListTile(
              title: const Text("Artist",
                style: TextStyle(fontSize: 20,fontWeight: FontWeight.bold),
              ),
              leading: const Icon(CupertinoIcons.rectangle_stack_person_crop),
              trailing: const Icon(CupertinoIcons.chevron_right),
              onTap: (){
                onTapItem!();
              },
            ),
          ],
        ),
      ),
    );
  }
}
