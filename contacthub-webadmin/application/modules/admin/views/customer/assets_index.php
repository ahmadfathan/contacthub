<?php
add_assets_header('<link rel="stylesheet" href="'.theme_path("plugins/select2/css/select2.min.css").'">');
add_assets_header('<link rel="stylesheet" href="'.theme_path("plugins/select2-bootstrap4-theme/select2-bootstrap4.min.css").'">');

add_assets_footer('<script src="'.theme_path("plugins/select2/js/select2.full.min.js").'"></script>');
add_assets_header('<link rel="stylesheet" href="'.theme_path("plugins/datatables-bs4/css/dataTables.bootstrap4.css").'">');

add_assets_footer('<script src="'.theme_path("plugins/datatables/jquery.dataTables.js").'"></script>');
add_assets_footer('<script src="'.theme_path("plugins/datatables-bs4/js/dataTables.bootstrap4.js").'"></script>');

add_assets_footer($app->load_view_content('customer/script_footer_index',$param));