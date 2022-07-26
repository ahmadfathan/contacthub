<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title><?= title() ?></title>
    <!-- Tell the browser to be responsive to screen width -->
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <!-- Font Awesome -->
    <link rel="stylesheet" href="<?= theme_path('plugins/fontawesome-free/css/all.min.css') ?>">
    <!-- Ionicons -->
    <link rel="stylesheet" href="https://code.ionicframework.com/ionicons/2.0.1/css/ionicons.min.css">
    <!-- overlayScrollbars -->
    <link rel="stylesheet" href="<?= theme_path('dist/css/adminlte.min.css') ?>">
    <!-- Google Font: Source Sans Pro -->
    <link href="https://fonts.googleapis.com/css?family=Source+Sans+Pro:300,400,400i,700" rel="stylesheet">
    <style>
        .modal-loading {
        display:    none;
        position:   fixed;
        z-index:    1000;
        top:        0;
        left:       0;
        height:     100%;
        width:      100%;
        background: rgba( 255, 255, 255, .8 )
                    url('<?= base_url("assets/img/loading.gif") ?>')
                    50% 50%
                    no-repeat;
        }

        /* When the body has the loading class, we turn
        the scrollbar off with overflow:hidden */
        body.loading {
            overflow: hidden;
        }

        /* Anytime the body has the loading class, our
        modal element will be visible */
        body.loading .modal-loading {
            display: block;
        }
    </style>

    <!-- jQuery -->
    <?= assets_head() ?>
</head>
<body class="hold-transition sidebar-mini">
<div class="modal-loading"></div>
    <!-- Site wrapper -->
    <div class="wrapper">

        <!-- Navbar -->
        <nav class="main-header navbar navbar-expand navbar-white navbar-light">
            <!-- Left navbar links -->
            <ul class="navbar-nav">
                <li class="nav-item">
                    <a class="nav-link" data-widget="pushmenu" href="#"><i class="fas fa-bars"></i></a>
                </li>
            </ul>

            <!-- Right navbar links -->
            <ul class="navbar-nav ml-auto">
                
                <li class="nav-item dropdown user user-menu">
                    <a href="#" class="nav-link"  data-toggle="dropdown" aria-expanded="true">
                        <img src="<?= base_url('assets/img/avatar-blank.jpg') ?>" class="user-image">
                    </a>
                    <div class="dropdown-menu dropdown-menu-lg dropdown-menu-right">
                        <span class="dropdown-item">
                            <!-- Message Start -->
                            <div class="media">
                                <img src="<?= base_url('assets/img/avatar-blank.jpg') ?>" alt="User Avatar" class="img-size-50 img-circle mr-3">
                                <div class="media-body">
                                    <h3 class="dropdown-item-title">
                                        <?= $this->user->Nickname ?>
                                    </h3>
                                    <p class="text-sm"><?= $this->user->Email ?></p>
                                    <p class="text-sm text-muted"><i class="far fa-user mr-1"></i> <?= $this->user->Role->Name ?></p>
                                </div>
                            </div>
                            <!-- Message End -->
                        </span>
                        <a href="<?= base_url('profile') ?>" class="dropdown-item">
                            <i class="fas fa-user mr-2"></i> Ubah Profile
                        </a>
                        <div class="dropdown-divider"></div>
                        <a href="<?= base_url('profile/ubahpassword') ?>" class="dropdown-item">
                            <i class="fas fa-lock mr-2"></i> Ubah Password
                        </a>
                        <div class="dropdown-divider"></div>
                        <?= anchor('logout','<i class="fas fa-sign-out-alt mr-2"></i> Logout',array('class'=>'dropdown-item')) ?>
                    </div>

                </li>
            </ul>
        </nav>
        <!-- /.navbar -->

        <!-- Main Sidebar Container -->
        <aside class="main-sidebar sidebar-dark-primary elevation-4">
            <!-- Brand Logo -->
            <a href="<?= base_url('auth') ?>" class="brand-link">
                <img src="<?= base_url('assets/img/logo.png') ?>"
                alt="Logo Kontak Hub"
                class="brand-image img-circle elevation-3"
                style="opacity: .8">
                <span class="brand-text font-weight-light">Kontak Hub</span>
            </a>

            <!-- Sidebar -->
            <div class="sidebar">
                <!-- Sidebar user (optional) -->
                <div class="user-panel mt-3 pb-3 mb-3 d-flex">
                    <div class="image">
                        <img src="<?= base_url('assets/img/avatar-blank.jpg') ?>" class="img-circle elevation-2" alt="User Image">
                    </div>
                    <div class="info">
                        <a href="#" class="d-block"><?= $this->user->Nickname ?></a>
                    </div>
                </div>

                <!-- Sidebar Menu -->
                <nav class="mt-2">
                    <?php echo $this->dynamic_menu->build_menu(); ?>
                </nav>
                <!-- /.sidebar-menu -->
            </div>
            <!-- /.sidebar -->
        </aside>

        <!-- Content Wrapper. Contains page content -->
        <div class="content-wrapper">
            <?php $this->load->view(@$content) ?>
        </div>
        <!-- /.content-wrapper -->

        <footer class="main-footer">
            <div class="float-right d-none d-sm-block">
                <?= app_version() ?>
            </div>
            <?= app_copyright() ?>
        </footer>
    </div>
    <?= get_modals() ?>
    <!-- ./wrapper -->
    <script src="<?= theme_path('plugins/jquery/jquery.min.js') ?>"></script>
    <!-- Bootstrap 4 -->
    <script src="<?= theme_path('plugins/bootstrap/js/bootstrap.bundle.min.js') ?>"></script>
    <!-- AdminLTE App -->
    <script src="<?= theme_path('dist/js/adminlte.min.js') ?>"></script>
    <!-- AdminLTE for demo purposes -->
    <script src="<?= theme_path('dist/js/demo.js') ?>"></script>


    <script type="text/javascript">
        $body = $("body");

        $(document).on({
            ajaxStart: function() { $body.addClass("loading");    },
            ajaxStop: function() { $body.removeClass("loading"); }
        });
    </script>
    <?= assets_footer() ?>
</body>
</html>
