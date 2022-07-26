<!-- Content Header (Page header) -->
<section class="content-header">
    <div class="container-fluid">
        <div class="row mb-2">
            <div class="col-sm-6">
                <h1><?= title() ?></h1>
            </div>
            <div class="col-sm-6">
                <ol class="breadcrumb float-sm-right">
                    <li class="breadcrumb-item"><?= anchor($action_home,lang('label_home')) ?></li>
                    <li class="breadcrumb-item active"><?= $breadcrumb_active ?></li>
                </ol>
            </div>
        </div>
    </div><!-- /.container-fluid -->
</section>

<!-- Main content -->
<section class="content">
    <div class="row">
        <div class="col-12">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title"><?= lang('label_auto_save_contact') ?></h3>
                </div>
                <!-- /.card-header -->
                <div class="card-body">
                    Klik Run untuk menjalankan Auto Save
                </div>
                <div class="card-footer">
                    <button class="btn btn-primary" onclick="run_autosave()"><?= lang('label_run') ?></button>
                </div>
            </div>
        </div>
    </div>
</section>
