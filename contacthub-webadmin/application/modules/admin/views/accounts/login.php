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
    <!-- icheck bootstrap -->
    <link rel="stylesheet" href="<?= theme_path('plugins/icheck-bootstrap/icheck-bootstrap.min.css') ?>">
    <!-- Theme style -->
    <link rel="stylesheet" href="<?= theme_path('dist/css/adminlte.min.css') ?>">
    <!-- Google Font: Source Sans Pro -->
    <link href="https://fonts.googleapis.com/css?family=Source+Sans+Pro:300,400,400i,700" rel="stylesheet">
</head>
<body class="hold-transition login-page">
    <div class="login-box">
        <div class="login-logo" style="margin-bottom:30px">
            <img src="<?= base_url('assets/img/logo.png') ?>" width="80px" alt="KontakHub Logo"><br>
            <a href="<?= base_url('auth') ?>"><b>KontakHub</b> Admin</a>
        </div>
        <?= show_alert() ?>
        <!-- /.login-logo -->
        <div class="card">
            <div class="card-body login-card-body">

                <p class="login-box-msg"><?= lang('label_attention_login') ?></p>

                <?= form_open('auth/login',array('method'=>'post')) ?>
                <div class="input-group mb-3">
                    <?= form_input(['name'=>'username','type'=>'text','class'=>'form-control','placeholder'=> lang('label_placeholder_username_login'),'required'=>true]) ?>
                    <div class="input-group-append">
                        <div class="input-group-text">
                            <span class="fas fa-envelope"></span>
                        </div>
                    </div>
                </div>
                <div class="input-group mb-3">
                    <?= form_input(['name'=>'password','type'=>'password','class'=>'form-control','placeholder'=>lang('label_placeholder_password_login'),'required'=>true]) ?>
                    <div class="input-group-append">
                        <div class="input-group-text">
                            <span class="fas fa-lock"></span>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-8">
                        <div class="icheck-primary">
                            <?= form_input(['name'=>'remember','type'=>'checkbox','id'=>'remember']) ?>
                            <label for="remember">
                               <?= lang('label_remember_me') ?>
                            </label>
                        </div>
                    </div>
                    <!-- /.col -->
                    <div class="col-4">
                        <?= form_button(['type'=>'submit','class'=>'btn btn-primary btn-block','content'=>lang('label_login')]) ?>
                    </div>
                    <!-- /.col -->
                </div>
                <?= form_close(); ?>
                <!-- /.social-auth-links -->

                <p class="mb-1">
                    <!-- <?= anchor('auth/forgot',lang('label_forgot_password')) ?> -->
                </p>
            </div>
            <!-- /.login-card-body -->
        </div>
    </div>
    <!-- /.login-box -->

    <!-- jQuery -->
    <script src="<?= theme_path('plugins/jquery/jquery.min.js') ?>"></script>
    <!-- Bootstrap 4 -->
    <script src="<?= theme_path('plugins/bootstrap/js/bootstrap.bundle.min.js') ?>"></script>
    <!-- AdminLTE App -->
    <script src="<?= theme_path('dist/js/adminlte.min.js') ?>"></script>

</body>
</html>
