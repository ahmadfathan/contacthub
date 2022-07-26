<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class Authentication extends Admin_Controller {
    public function __construct(){
        parent::__construct();
        $this->load->model('api/api_auth');
    }
    public function index()
	{
        $this->auth();
        $this->title = "Login";
        $this->init();
        $this->load->view('admin/accounts/login');
	}
    public function login(){

        $this->auth();
        $username      = $this->input->post('username',TRUE);
        $remember   = $this->input->post('remember',TRUE);
        $password   = $this->input->post('password',TRUE);

        if ($this->empty($username)){
            create_alert(['type'=>'danger','message'=>'Username / Email tidak boleh kosong']);
        }else if ($this->empty($password)){
            create_alert(['type'=>'danger','message'=>'Password tidak boleh kosong']);
        }else{
            $body = [
                'Email' => $username,
                'Password' => $password,
                'Platform' => 'web'
            ];
            $login = $this->api_auth->login($body);
            if ($login->status == 'OK' && $login->result->RoleId == 1){
                $session = array(
					$this->_IS_LOGGEDIN => true,
					$this->_ROLE		=> $login->result->Role,
					$this->_U_ID		=> $login->result->UserId,
                    $this->_API_KEY		=> $login->result->Auth->Key,
				);
				if ($remember){
					$this->config->set_item('sess_expiration', '14400');
				}
                $this->session->set_userdata($session);
            }else{
                $message = $login->message;
                create_alert(['type'=>'danger','message'=>$message]);
            }
        }
        redirect('auth');
    }
    public function logout(){
        $login = $this->api_auth->logout($this->session->userdata($this->_API_KEY));
        $this->session->sess_destroy();
        redirect('auth');
    }
}
