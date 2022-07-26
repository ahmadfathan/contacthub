<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class Settings extends Admin_Controller {

	public function __construct(){
        parent::__construct();
        $this->load->model('admin/api/Api_settings');
		$this->auth();
	}
	public function index()
	{
		$this->title 	= lang("label_data_settings");
		$this->assets 	= array('assets_index');
        $this->content  = "settings/index";
        
        $params = [
            'action_home' => 'dashboard',
            'action_add' => 'settings/add',
            'breadcrumb_active' => lang('label_settings')
        ];
		$this->template($params);
    }
    public function edit($id){
		$this->title 	= lang('label_edit_settings');
		$this->assets 	= array('assets_form');
		$this->content  = "settings/edit";

        $data['Result'] = $this->Api_settings->getAll(['SettingsId' => $id]);

		if (empty($data['Result']->result) || (is_array($data['Result']->result) && count($data['Result']->result) == 0)){
			create_alert(['type'=>'danger','message'=> $data['Result']->message]);
			redirect('settings');
        }
        $data['Settings'] = $data['Result']->result[0];
        if ($data['Settings']->SettingsId == 'CS_CONTACT') {
		    $this->content  = "settings/edit-cs-contact";
            $data_settings = [];
            if (is_array(json_decode($data['Settings']->Value)) && count(json_decode($data['Settings']->Value)) > 0){
                foreach (json_decode($data['Settings']->Value) as $item) {
                    $data_settings[] = [
                        "Name" => $item->Name,
                        "Value" => $item->Phone,
                    ];
                }
                $data['Settings']->Value = $data_settings;
            }else{
                $data['Settings']->Value = [
                    ["Name" => "","Value" => ""],
                    ["Name" => "","Value" => ""],
                    ["Name" => "","Value" => ""],
                    ["Name" => "","Value" => ""],
                    ["Name" => "","Value" => ""],
                    ["Name" => "","Value" => ""]
                ];
            }
        }else if ($data['Settings']->SettingsId == 'TEMPLATE_SHARE') {
		    $this->content  = "settings/edit-template-share";
        }else{
		    $this->content  = "settings/edit-value";
        }
        $param = array(
            'action_home' => 'dashboard',
            'action_save' => 'settings/update/'.$id,
            'action_cancel' => 'settings',
            'data' => $data,
            'breadcrumb_active' => lang('label_settings')
		);
		$this->template($param);
	}
    public function update($id){
        $data = [];
        $data['SettingsId'] = $id;
        if ($id=='CS_CONTACT'){
            $data_settings = [];
            $Name = $this->input->post('Name[]',TRUE);
            $Value = $this->input->post('Value[]',TRUE);
            foreach ($Name as $item) {
                if (empty(trim($item)) == false){
                    $data_settings[]['Name'] = $item;
                }
            }
            $i = 0;
            foreach ($Value as $item) {
                if ($i < count($data_settings)){
                    $data_settings[$i]['Phone'] = $item;
                }
                $i++;
            }
            $data['Value'] = json_encode($data_settings);
        }else{
            $data['Value'] 		= $this->input->post('Value',TRUE);
        }
            
        $save = $this->Api_settings->update($data);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }
        redirect('settings/edit/'.$id);
    }
    
    public function ajax_list(){
        $param = [];
        $url_edit = 'settings/edit/';

        if ($_POST['search']['value']){
            $param['keyword'] = $_POST['search']['value'];
        }
        $param['numberPage'] = (int) $_POST['length'];
        $param['page'] = (int) $_POST['start'];
        if ($param['page']==0){
            $param['page'] = 1;
        }else{
            $param['page'] = $param['page'] / $param['numberPage'] + 1 ;
        }
        if (isset($_POST['order'])){
            $param['order_column'] = $_POST['order']['0']['column'];
            $param['order_dir'] = $_POST['order']['0']['dir'];
        }
        $list = $this->Api_settings->get_datatables($param);
        $data = array();

        $no = $_POST['start'];
        $dataTables = array();
        foreach ($list->result->data as $data) {
            $id = $data->SettingId;
           
            $btngroup = create_button_group([
                'content' => lang('label_action'),
                // 'attr'=>array('disabled'=> ($data->is_system) ? true:false),
                'li' => array(
                    ['type'=>'a','content'=>'<i class="fa fa-edit"></i> '.lang('label_edit'),'attr'=>array('href'=>$url_edit.$id)]
                )
            ]);

            $no++;
            $row = array();
            $row[] = $btngroup;
            $row[] = lang($data->SettingId);
            $row[] = $data->Value;
            $dataTables[] = $row;
        }

        $output = array(
            "draw" => $_POST['draw'],
            "recordsTotal" => $list->result->total,
            "recordsFiltered" => $list->result->total,
            "data" => $dataTables,
        );
        //output to json format
        echo json_encode($output);
    }
}
