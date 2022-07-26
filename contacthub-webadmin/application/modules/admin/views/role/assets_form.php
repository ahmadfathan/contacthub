<?php

add_assets_header('<link rel="stylesheet" href="'.theme_path("plugins/bootstrap4-duallistbox/bootstrap-duallistbox.min.css").'">');

add_assets_footer('<script src="'.theme_path("plugins/bootstrap4-duallistbox/jquery.bootstrap-duallistbox.min.js").'"></script>');
add_assets_footer($app->load_view_content('role/script_footer_form',$param));
