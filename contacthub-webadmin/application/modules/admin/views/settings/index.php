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
            <?= show_alert() ?>
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title"><?= lang('label_settings') ?></h3>
                </div>
                <!-- /.card-header -->
                <div class="card-body">
                    <table id="datatables" class="table table-bordered table-hover">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th><?= lang('label_setting_id') ?></th>
                                <th><?= lang('label_value') ?></th>
                            </tr>
                        </thead>
                    </table>
                </div>
                <div class="card-footer">
                    <?= anchor($action_add,lang('label_add'),array('class'=>'btn btn-primary')) ?>
                </div>
            </div>
        </div>
    </div>
</section>
