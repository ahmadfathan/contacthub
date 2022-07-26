<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class Profile extends Admin_Controller {

	public function __construct(){
        parent::__construct();
        $this->load->model('admin/api/Api_user');
        $this->load->model('admin/api/Api_role');
		$this->auth();
	}
	public function index()
	{
		$this->title 	= lang("label_profile");
		$this->assets 	= array('assets_index');
        $this->content  = "profile/index";
        $id = $this->user->UserId;

        $data['result'] = $this->Api_user->by_id($id)->result;
		if (empty($data['result']) || (is_array($data['result']) && count($data['result']) == 0)){
			create_alert(['type'=>'danger','message'=> lang('attention_not_found')]);
			redirect('profile');
        }
        $data['user'] = $data['result'];
        $param = array(
            'action_home' => 'dashboard',
            'action_save' => 'profile/update',
            'action_cancel' => 'profile',
            'data' => $data,
            'breadcrumb_active' => lang('label_profile')
		);
		$this->template($param);
    }
    public function ubahpassword(){
		$this->title 	= lang('label_ubah_password');
		$this->assets 	= array('assets_form');
        $this->content  = "profile/ubahpassword";
        
		$param = array(
            'action_home' => 'dashboard',
            'action_save' => 'profile/update/password',
            'action_cancel' => 'profile',
            'breadcrumb_active' => lang('label_profile')
		);
		$this->template($param);
    }
    public function updatepassword(){
        $id = $this->user->UserId;
        $data = [];
        $data['LastPassword'] 		= $this->input->post('LastPassword',TRUE);
        $data['NewPassword'] 		= $this->input->post('NewPassword',TRUE);
        $config = [
            ['field' => 'LastPassword','label' => lang('label_last_password'),'rules'	=> rules_validation('no_rules',true)],
            ['field' => 'NewPassword','label' => lang('label_new_password'),'rules'	=> rules_validation('no_rules',true)],
        ];
        $this->form_validation->set_rules($config);
        if ($this->form_validation->run() == FALSE){
            create_alert(['type'=>'danger','message'=>validation_errors()]);
        }else{
            $save = $this->Api_user->update_password($id,$data);
            if($save->status=='OK'){
                create_alert(['type'=>'success','message'=>$save->message]);
            }else{
                create_alert(['type'=>'danger','message'=>$save->message]);
            }

        }
        redirect('profile/ubahpassword');
    }
    public function update(){
        $id = $this->user->UserId;
        $data = [];
        $data['Username'] 		= $this->input->post('Username',TRUE);
        $data['Nickname'] 		= $this->input->post('Nickname',TRUE);
        $data['Email'] 		    = $this->input->post('Email',TRUE);
        $config = [
            ['field' => 'Username','label' => lang('label_username'),'rules'	=> rules_validation('no_rules',true)],
            ['field' => 'Nickname','label' => lang('label_nickname'),'rules'	=> rules_validation('no_rules',true)],
            ['field' => 'Email','label' => lang('label_email'),'rules'	=> rules_validation('no_rules',true)]
        ];
        $this->form_validation->set_rules($config);
        if ($this->form_validation->run() == FALSE){
            create_alert(['type'=>'danger','message'=>validation_errors()]);
        }else{
            $save = $this->Api_user->update($id,$data);
            if($save->status=='OK'){
                create_alert(['type'=>'success','message'=>$save->message]);
            }else{
                create_alert(['type'=>'danger','message'=>$save->message]);
            }

        }
        redirect('profile');
    }
    
}
