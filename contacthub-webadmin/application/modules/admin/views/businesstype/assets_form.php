<?php
add_assets_header('<link rel="stylesheet" href="'.theme_path("plugins/select2/css/select2.min.css").'">');
add_assets_header('<link rel="stylesheet" href="'.theme_path("plugins/select2-bootstrap4-theme/select2-bootstrap4.min.css").'">');

add_assets_footer('<script src="'.theme_path("plugins/select2/js/select2.full.min.js").'"></script>');
add_assets_footer('<script src="'.theme_path("plugins/inputmask/min/jquery.inputmask.bundle.min.js").'"></script>');

add_assets_footer($app->load_view_content('businesstype/script_footer_form',$param));