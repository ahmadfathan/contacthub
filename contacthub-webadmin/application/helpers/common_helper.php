<?php
function convert_bulan($bln){
	switch ($bln) {
		case '1': return 'Jan';
		case '2': return 'Feb';
		case '3': return 'Mar';
		case '4': return 'Apr';
		case '5': return 'Mei';
		case '6': return 'Jun';
		case '7': return 'Jul';
		case '8': return 'Agu';
		case '9': return 'Sep';
		case '10': return 'Okt';
		case '11': return 'Nov';
		case '12': return 'Des';
		
		default:
		return false;
			break;
	}
}
function add_assets_header($assets){
	$CI =& get_instance();
	$tmp = $CI->config->item('fs_assets_header');
	$assets = $CI->config->set_item('fs_assets_header',$tmp.$assets);
}
function add_modals($assets){
	$CI =& get_instance();
	$tmp = $CI->config->item('fs_modals');
	$assets = $CI->config->set_item('fs_modals',$tmp.$assets);
}
function add_assets_footer($assets){
	$CI =& get_instance();
	$tmp = $CI->config->item('fs_assets_footer');
	$assets = $CI->config->set_item('fs_assets_footer',$tmp.$assets);
}
function assets_head(){
	$CI =& get_instance();
	$assets = $CI->config->item('fs_assets_header');
	return $assets;
}
function get_modals(){
	$CI =& get_instance();
	$assets = $CI->config->item('fs_modals');
	return $assets;
}
function get_vars(){
	$CI =& get_instance();
	$assets = $CI->config->item('fs_vars');
	return $assets;
}
function assets_footer(){
	$CI =& get_instance();
	$assets = $CI->config->item('fs_assets_footer');
	return $assets;
}
function title(){
	$CI =& get_instance();
	$title = $CI->config->item('fs_title');
	return $title;
}

function theme_path($filename = ""){
	$CI =& get_instance();
	$config = $CI->config->item('fs_theme_path');
	$config = $config."/";
	if (!($filename == "" || empty($filename || $filename === NULL))) {
		$config = $config."/".$filename;
	}
	return $config;
}
function app_hash($val){
    return hash("sha256",sha1($val));
}
function app_copyright(){
	return '<strong>Copyright &copy; 2020 <a href="#">Kontak Hub</a>. </strong>All rights reserved.';
}
function app_version(){
	return '1.0.0';
}
function create_alert($config){

	$CI =& get_instance();

	$alert = '<div class="box-body">
	  <div class="alert alert-'.$config["type"].' alert-dismissible">
	    <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
	    '.$config["message"].'
	  </div>
	</div>';
	$CI->session->set_flashdata('notification',$alert);
}
function show_alert(){
	$CI =& get_instance();
	echo $CI->session->flashdata('notification');
}

function label_budge($config){
	return '<span class="badge badge-'.$config['type'].' badge-pill">'.$config['text'].'</span>';
}
function label_skin($config){
	return '<label class="btn-xs btn-'.$config['type'].'">'.$config['text'].'</label>';
}
function time_elapsed_string($datetime, $full = false) {
    $now = new DateTime;
    $ago = new DateTime($datetime);
    $diff = $now->diff($ago);

    $diff->w = floor($diff->d / 7);
    $diff->d -= $diff->w * 7;

    $string = array(
        'y' => 'year',
        'm' => 'month',
        'w' => 'week',
        'd' => 'day',
        'h' => 'hour',
        'i' => 'minute',
        's' => 'second',
    );
    foreach ($string as $k => &$v) {
        if ($diff->$k) {
            $v = $diff->$k . ' ' . $v . ($diff->$k > 1 ? 's' : '');
        } else {
            unset($string[$k]);
        }
    }

    if (!$full) $string = array_slice($string, 0, 1);
    return $string ? implode(', ', $string) . ' ago' : 'just now';
}
function convertTglID($date,$skin=false){
	$time = strtotime($date);
	$day  = date('d',$time);
	$month  = date('m',$time);
	$year  = date('Y',$time);
	$h  = date('h',$time);
	$m  = date('i',$time);
	$s  = date('s',$time);

	$text = $day.'-'.$month.'-'.$year.' '.$h.':'.$m.':'.$s;
	if ($skin){
		return label_skin(['type'=>'default','text'=>$text]);
	}else{
		return $text;
	}
}
function convertDateID($date,$skin=false){
	$time = strtotime($date);
	$day  = date('d',$time);
	$month  = date('m',$time);
	$year  = date('Y',$time);

	$text = $day.' '.convert_bulan($month).' '.$year;
	if ($skin){
		return label_skin(['type'=>'default','text'=>$text]);
	}else{
		return $text;
	}
}
function convertDateTimeID($date,$skin=false){
	$time = strtotime($date);
	$day  = date('d',$time);
	$month  = date('m',$time);
	$year  = date('Y',$time);
	$hour  = date('H',$time);
	$minutes  = date('i',$time);
	$seconds  = date('s',$time);

	$text = $day.' '.convert_bulan($month).' '.$year.' '.$hour.':'.$minutes.':'.$seconds;
	if ($skin){
		return label_skin(['type'=>'default','text'=>$text]);
	}else{
		return $text;
	}
}
function isEmpty($val){
	if (($val === NULL || empty($val)) && $val != 0 ){
		return TRUE;
	}else{
		return FALSE;
	}
}
function kirim_email($data){
	$ci = &get_instance();
	$to = $data['to'];
	$body = $data['body'];
	$subject = $data['subject'];
	$config = [
            'mailtype'  => 'html',
            'charset'   => 'utf-8',
            'protocol'  => 'smtp',
            'smtp_host' => 'ssl://smtp.googlemail.com',
            'smtp_user' => 'untuktestingemail@gmail.com',  // Email gmail
            'smtp_pass'   => 'hanyauntuktesting',  // Password gmail
            'smtp_crypto' => 'ssl',
            'smtp_port'   => 465,
            'crlf'    => "\r\n",
            'newline' => "\r\n"
        ];
	$ci->load->library('email',$config);
    $ci->email->from('untuktestingemail@gmail.com', 'Email Testing');
    $ci->email->to($to);
    $ci->email->subject($subject);
    $ci->email->message($body);

    if ($ci->email->send()) {
        return true;
    } else {
        return false;
    }
}
function random_strings($length_of_string)
{

    // String of all alphanumeric character
    $str_result = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz';

    // Shufle the $str_result and returns substring
    // of specified length
    return substr(str_shuffle($str_result),
                       0, $length_of_string);
}
function create_button_group($array){
	$content 	= (isset($array['content'])) ? $array['content'] : null;
	$attr 		= (isset($array['attr'])) ? $array['attr'] : null;
	$disabled   = (@$attr["disabled"]==true) ? "disabled":"";
	$li 		= $array['li'];
	$html_out 	= '<div class="input-group-prepend input-xs">';
	$html_out  .= '<button type="button" class="btn btn-default dropdown-toggle btn-sm" data-toggle="dropdown" aria-expanded="false" '.$disabled.'>';
	$html_out  .= $content;
	$html_out  .= '</button>';
	if ($disabled){

	}else{
		$html_out  .= '<div class="dropdown-menu" x-placement="bottom-start" style="position: absolute; will-change: transform; top: 0px; left: 0px; transform: translate3d(0px, 38px, 0px);">';
		for ($i=0; $i < count($li); $i++) {
			if (is_array($li[$i])){
				$type = $li[$i]['type'];
				if(strtolower(trim($type))!='divider'){
					$content = $li[$i]['content'];
					$attr = $li[$i]['attr'];
					if (isset($attr['class'])){
						$attr['class'] .= ' dropdown-item';
					}else{
						$attr['class'] = 'dropdown-item';
					}
				}
				if (strtolower(trim($type))=='a'){
					$href = (isset($attr['href'])) ? ($attr['href']=='#') ? $attr['href'] = "javascript:void(0)" : $attr['href'] : $attr['href']="javascript:void(0)";
					$html = anchor($href,$content,$attr);
				}else if(strtolower(trim($type))=='divider'){
					$html = '<div class="dropdown-divider"></div>';
				}else if(strtolower(trim($type))=='button'){
					$data = $attr;
					$data['content'] = $content;
					$html = form_button($data);
				}
			}else{
				$html = $li[$i];
			}
			$html_out .= '<li>'.$html.'</li>';
		}
		$html_out .= '</div>';
	}

	return $html_out .= '</div>';
}
function status_standard($status,$skin=false){
	switch ($status) {
		case '0':$result = "Tidak aktif";$type = "danger";break;
		case '1':$result = "Aktif";$type = "success";break;
		default:
			$result = "Tidak aktif";
			$type = "danger";
			break;
	}
	if ($skin){
		return label_skin(['type'=>$type,'text'=>$result]);
	}else{
		return $result;
	}
}
function status_user($status,$skin=false){
	switch ($status) {
		case '0':$result = "Pending";$type = "default";break;
		case '1':$result = "Tidak aktif";$type = "danger";break;
		case '2':$result = "Aktif";$type = "success";break;
		case '3':$result = "Blokir";$type = "warning";break;
		default:
			$result = "Pending";
			$type = "default";
			break;
	}
	if ($skin){
		return label_skin(['type'=>$type,'text'=>$result]);
	}else{
		return $result;
	}
}
function array_status_user(){
	return ['0'=>'Pending','1'=>'Tidak aktif','2'=>'Aktif','3'=>'Blokir'];
}

function form_validation_error($error){
    foreach ($error as $key => $value) {
        return ['key'=>$key,'value'=>$value];
    }
}
function error_code($key){
    return pad_error_code(lang('code_'.$key));
}
function pad_error_code($err){
    return str_pad($err,5,'0',STR_PAD_LEFT);
}
function rules_validation($key,$required = false){
    $ci =& get_instance();
    $rules = $ci->config->item('rules_validation');
    $result = @$rules[$key];
    if ($required){
        if (empty($result) == false){
            $result .= "|";
        }
        $result .= 'required';
    }
    return $result;
}
function get_font_awesome(){
	return ["ad","address-book","address-card","adjust","air-freshener","align-center","align-justify","align-left","align-right","allergies","ambulance","american-sign-language-interpreting","anchor","angle-double-down","angle-double-left","angle-double-right","angle-double-up","angle-down","angle-left","angle-right","angle-up","angry","ankh",
	"apple-alt","archive","archway","arrow-alt-circle-down","arrow-alt-circle-left","arrow-alt-circle-right","arrow-alt-circle-up","arrow-circle-down","arrow-circle-left","arrow-circle-right","arrow-circle-up","arrow-down","arrow-left","arrow-right","arrow-up","arrows-alt","arrows-alt-h","arrows-alt-v","assistive-listening-systems","asterisk","at","atlas","atom","audio-description","award",
	"baby","baby-carriage","backspace","backward","bacon","bahai","balance-scale","balance-scale-left","balance-scale-right","ban","band-aid","barcode","bars","baseball-ball","basketball-ball","bath","battery-empty","battery-full","battery-half","battery-quarter","battery-three-quarters","bed","beer","bell","bell-slash",
	"bezier-curve","bible","bicycle","biking","binoculars","biohazard","birthday-cake","blender","blender-phone","blind","blog","bold","bolt","bomb","bone","bong","book","book-dead","book-medical","book-open","book-reader","bookmark","border-all","border-none","border-style",
	"bowling-ball","box","box-open","boxes","braille","brain","bread-slice","briefcase","briefcase-medical","broadcast-tower","broom","brush","bug","building","bullhorn","bullseye","burn","bus","bus-alt","business-time","calculator","calendar","calendar-alt","calendar-check","calendar-day",
	"calendar-minus","calendar-plus","calendar-times","calendar-week","camera","camera-retro","campground","candy-cane","cannabis","capsules","car","car-alt","car-battery","car-crash","car-side","caravan","caret-down","caret-left","caret-right","caret-square-down","caret-square-left","caret-square-right","caret-square-up","caret-up","carrot",
	"cart-arrow-down","cart-plus","cash-register","cat","certificate","chair","chalkboard","chalkboard-teacher","charging-station","chart-area","chart-bar","chart-line","chart-pie","check","check-circle","check-double","check-square","cheese","chess","chess-bishop","chess-board","chess-king","chess-knight","chess-pawn","chess-queen",
	"chess-rook","chevron-circle-down","chevron-circle-left","chevron-circle-right","chevron-circle-up","chevron-down","chevron-left","chevron-right","chevron-up","child","church","circle","circle-notch","city","clinic-medical","clipboard","clipboard-check","clipboard-list","clock","clone","closed-captioning","cloud","cloud-download-alt","cloud-meatball","cloud-moon",
	"cloud-moon-rain","cloud-rain","cloud-showers-heavy","cloud-sun","cloud-sun-rain","cloud-upload-alt","cocktail","code","code-branch","coffee","cog","cogs","coins","columns","comment","comment-alt","comment-dollar","comment-dots","comment-medical","comment-slash","comments","comments-dollar","compact-disc","compass","compress",
	"compress-alt","compress-arrows-alt","concierge-bell","cookie","cookie-bite","copy","copyright","couch","credit-card","crop","crop-alt","cross","crosshairs","crow","crown","crutch","cube","cubes","cut","database","deaf","democrat","desktop","dharmachakra","diagnoses",
	"dice","dice-d20","dice-d6","dice-five","dice-four","dice-one","dice-six","dice-three","dice-two","digital-tachograph","directions","divide","dizzy","dna","dog","dollar-sign","dolly","dolly-flatbed","donate","door-closed","door-open","dot-circle","dove","download","drafting-compass",
	"dragon","draw-polygon","drum","drum-steelpan","drumstick-bite","dumbbell","dumpster","dumpster-fire","dungeon","edit","egg","eject","ellipsis-h","ellipsis-v","envelope","envelope-open","envelope-open-text","envelope-square","equals","eraser","ethernet","euro-sign","exchange-alt","exclamation","exclamation-circle",
	"exclamation-triangle","expand","expand-alt","expand-arrows-alt","external-link-alt","external-link-square-alt","eye","eye-dropper","eye-slash","fan","fast-backward","fast-forward","fax","feather","feather-alt","female","fighter-jet","file","file-alt","file-archive","file-audio","file-code","file-contract","file-csv","file-download",
	"file-excel","file-export","file-image","file-import","file-invoice","file-invoice-dollar","file-medical","file-medical-alt","file-pdf","file-powerpoint","file-prescription","file-signature","file-upload","file-video","file-word","fill","fill-drip","film","filter","fingerprint","fire","fire-alt","fire-extinguisher","first-aid","fish",
	"fist-raised","flag","flag-checkered","flag-usa","flask","flushed","folder","folder-minus","folder-open","folder-plus","font","football-ball","forward","frog","frown","frown-open","funnel-dollar","futbol","gamepad","gas-pump","gavel","gem","genderless","ghost","gift",
	"gifts","glass-cheers","glass-martini","glass-martini-alt","glass-whiskey","glasses","globe","globe-africa","globe-americas","globe-asia","globe-europe","golf-ball","gopuram","graduation-cap","greater-than","greater-than-equal","grimace","grin","grin-alt","grin-beam","grin-beam-sweat","grin-hearts","grin-squint","grin-squint-tears","grin-stars",
	"grin-tears","grin-tongue","grin-tongue-squint","grin-tongue-wink","grin-wink","grip-horizontal","grip-lines","grip-lines-vertical","grip-vertical","guitar","h-square","hamburger","hammer","hamsa","hand-holding","hand-holding-heart","hand-holding-usd","hand-lizard","hand-middle-finger","hand-paper","hand-peace","hand-point-down","hand-point-left","hand-point-right","hand-point-up",
	"hand-pointer","hand-rock","hand-scissors","hand-spock","hands","hands-helping","handshake","hanukiah","hard-hat","hashtag","hat-cowboy","hat-cowboy-side","hat-wizard","hdd","heading","headphones","headphones-alt","headset","heart","heart-broken","heartbeat","helicopter","highlighter","hiking","hippo",
	"history","hockey-puck","holly-berry","home","horse","horse-head","hospital","hospital-alt","hospital-symbol","hot-tub","hotdog","hotel","hourglass","hourglass-end","hourglass-half","hourglass-start","house-damage","hryvnia","i-cursor","ice-cream","icicles","icons","id-badge","id-card","id-card-alt",
	"igloo","image","images","inbox","indent","industry","infinity","info","info-circle","italic","jedi","joint","journal-whills","kaaba","key","keyboard","khanda","kiss","kiss-beam","kiss-wink-heart","kiwi-bird","landmark","language","laptop","laptop-code",
	"laptop-medical","laugh","laugh-beam","laugh-squint","laugh-wink","layer-group","leaf","lemon","less-than","less-than-equal","level-down-alt","level-up-alt","life-ring","lightbulb","link","lira-sign","list","list-alt","list-ol","list-ul","location-arrow","lock","lock-open","long-arrow-alt-down","long-arrow-alt-left",
	"long-arrow-alt-right","long-arrow-alt-up","low-vision","luggage-cart","magic","magnet","mail-bulk","male","map","map-marked","map-marked-alt","map-marker","map-marker-alt","map-pin","map-signs","marker","mars","mars-double","mars-stroke","mars-stroke-h","mars-stroke-v","mask","medal","medkit","meh",
	"meh-blank","meh-rolling-eyes","memory","menorah","mercury","meteor","microchip","microphone","microphone-alt","microphone-alt-slash","microphone-slash","microscope","minus","minus-circle","minus-square","mitten","mobile","mobile-alt","money-bill","money-bill-alt","money-bill-wave","money-bill-wave-alt","money-check","money-check-alt","monument",
	"moon","mortar-pestle","mosque","motorcycle","mountain","mouse","mouse-pointer","mug-hot","music","network-wired","neuter","newspaper","not-equal","notes-medical","object-group","object-ungroup","oil-can","om","otter","outdent","pager","paint-brush","paint-roller","palette","pallet",
	"paper-plane","paperclip","parachute-box","paragraph","parking","passport","pastafarianism","paste","pause","pause-circle","paw","peace","pen","pen-alt","pen-fancy","pen-nib","pen-square","pencil-alt","pencil-ruler","people-carry","pepper-hot","percent","percentage","person-booth","phone",
	"phone-alt","phone-slash","phone-square","phone-square-alt","phone-volume","photo-video","piggy-bank","pills","pizza-slice","place-of-worship","plane","plane-arrival","plane-departure","play","play-circle","plug","plus","plus-circle","plus-square","podcast","poll","poll-h","poo","poo-storm","poop",
	"portrait","pound-sign","power-off","pray","praying-hands","prescription","prescription-bottle","prescription-bottle-alt","print","procedures","project-diagram","puzzle-piece","qrcode","question","question-circle","quidditch","quote-left","quote-right","quran","radiation","radiation-alt","rainbow","random","receipt","record-vinyl",
	"recycle","redo","redo-alt","registered","remove-format","reply","reply-all","republican","restroom","retweet","ribbon","ring","road","robot","rocket","route","rss","rss-square","ruble-sign","ruler","ruler-combined","ruler-horizontal","ruler-vertical","running","rupee-sign",
	"sad-cry","sad-tear","satellite","satellite-dish","save","school","screwdriver","scroll","sd-card","search","search-dollar","search-location","search-minus","search-plus","seedling","server","shapes","share","share-alt","share-alt-square","share-square","shekel-sign","shield-alt","ship","shipping-fast",
	"shoe-prints","shopping-bag","shopping-basket","shopping-cart","shower","shuttle-van","sign","sign-in-alt","sign-language","sign-out-alt","signal","signature","sim-card","sitemap","skating","skiing","skiing-nordic","skull","skull-crossbones","slash","sleigh","sliders-h","smile","smile-beam","smile-wink",
	"smog","smoking","smoking-ban","sms","snowboarding","snowflake","snowman","snowplow","socks","solar-panel","sort","sort-alpha-down","sort-alpha-down-alt","sort-alpha-up","sort-alpha-up-alt","sort-amount-down","sort-amount-down-alt","sort-amount-up","sort-amount-up-alt","sort-down","sort-numeric-down","sort-numeric-down-alt","sort-numeric-up","sort-numeric-up-alt","sort-up",
	"spa","space-shuttle","spell-check","spider","spinner","splotch","spray-can","square","square-full","square-root-alt","stamp","star","star-and-crescent","star-half","star-half-alt","star-of-david","star-of-life","step-backward","step-forward","stethoscope","sticky-note","stop","stop-circle","stopwatch","store",
	"store-alt","stream","street-view","strikethrough","stroopwafel","subscript","subway","suitcase","suitcase-rolling","sun","superscript","surprise","swatchbook","swimmer","swimming-pool","synagogue","sync","sync-alt","syringe","table","table-tennis","tablet","tablet-alt","tablets","tachometer-alt",
	"tag","tags","tape","tasks","taxi","teeth","teeth-open","temperature-high","temperature-low","tenge","terminal","text-height","text-width","th","th-large","th-list","theater-masks","thermometer","thermometer-empty","thermometer-full","thermometer-half","thermometer-quarter","thermometer-three-quarters","thumbs-down","thumbs-up",
	"thumbtack","ticket-alt","times","times-circle","tint","tint-slash","tired","toggle-off","toggle-on","toilet","toilet-paper","toolbox","tools","tooth","torah","torii-gate","tractor","trademark","traffic-light","trailer","train","tram","transgender","transgender-alt","trash",
	"trash-alt","trash-restore","trash-restore-alt","tree","trophy","truck","truck-loading","truck-monster","truck-moving","truck-pickup","tshirt","tty","tv","umbrella","umbrella-beach","underline","undo","undo-alt","universal-access","university","unlink","unlock","unlock-alt","upload","user",
	"user-alt","user-alt-slash","user-astronaut","user-check","user-circle","user-clock","user-cog","user-edit","user-friends","user-graduate","user-injured","user-lock","user-md","user-minus","user-ninja","user-nurse","user-plus","user-secret","user-shield","user-slash","user-tag","user-tie","user-times","users","users-cog",
	"utensil-spoon","utensils","vector-square","venus","venus-double","venus-mars","vial","vials","video","video-slash","vihara","voicemail","volleyball-ball","volume-down","volume-mute","volume-off","volume-up","vote-yea","vr-cardboard","walking","wallet","warehouse","water","wave-square","weight",
	"weight-hanging","wheelchair","wifi","wind","window-close","window-maximize","window-minimize","window-restore","wine-bottle","wine-glass","wine-glass-alt","won-sign","wrench","x-ray","yen-sign","yin-yang"];
}
