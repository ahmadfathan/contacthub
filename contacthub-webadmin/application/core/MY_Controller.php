<?php

if (!defined('BASEPATH'))
    exit('No direct script access allowed');

/* load the MX_Router class */
require APPPATH . "third_party/MX/Controller.php";

class MY_Controller extends MX_Controller
{

	function __construct()
	{
		parent::__construct();
		$this->_hmvc_fixes();
	}

	function _hmvc_fixes()
	{
		//fix callback form_validation
		//https://bitbucket.org/wiredesignz/codeigniter-modular-extensions-hmvc
		$this->load->library('form_validation');
		$this->form_validation->set_ci_reference( $this ); 
	}

}

/* End of file MY_Controller.php */
/* Location: ./application/core/MY_Controller.php */
class Admin_Controller extends MX_Controller {
	public $auth 			= TRUE;
	public $layout 			= "template_admin";
    public $path_theme 		= "/assets/adminlte";
	public $path_assets     = "assets";
	public $content 		= "";
	public $title;
	public $id;
	public $_IS_LOGGEDIN 	= "is_loggedin";
	public $_ROLE 			= "role";
	public $_U_ID 			= "u_id";
	public $_API_KEY 		= "api_key";
	public $user;
	public $assets 			= array();
	public $current_class;
	public $current_method;
	public function __construct(){
		parent::__construct();
		$this->current_class 	= strtolower($this->router->fetch_class());
		$this->current_method = strtolower($this->router->fetch_method());
		$this->load->model('admin/api/api_user');
		$this->load->model('admin/api/api_role');
		$this->init();
	}
	public function auth(){

		$loggedin 		= $this->session->userdata($this->_IS_LOGGEDIN);
		$role 			= $this->session->userdata($this->_ROLE);
		$id 			= $this->session->userdata($this->_U_ID);

		$this->api_key = $this->session->userdata($this->_API_KEY);
		if (@$loggedin == true){
			$this->user 	= (object) $this->api_user->by_id($id);
			if ($this->user->status!='OK'){
				show_error('403 Not Found','404',$this->user->message);
			}
			$this->user 	= (object) $this->user->result;
			if ($this->current_class=="authentication" && $this->current_method=="logout"){
				return false;
			}
			if ($this->check_role() == false or $role->IsAdministrator ==  false){

				// show_error('403 Forbidden','403','Tidak memiliki hak akses');
			}
		}else{
			if ($this->current_class=="authentication" /*|| ($this->current_class=="relawan" && $this->current_method=="print_ringkasan")*/){
				return false;
			}else{
				redirect('logout');
			}
		}
	}
	private function check_role(){
		if ($this->current_class=="authentication"){
			redirect('dashboard');
		}else{
			// $role_access = $this->api_role->access_role_list();
			// foreach ($this->user->Role->RoleAccess as $val) {
			// 	if (count($role_access->result) > 0){
			// 		foreach ($role_access->result as $val_access) {
			// 			if ($val_access->_id ==$val){
			// 				foreach ($val_access->ClassMethod as $class_method) {
			// 					if($class_method==$this->current_class.'/'.$this->current_method){
			// 						return true;
			// 					}
			// 				}
			// 				break;
			// 			}
			// 		}
			// 	}
			// }
		}
		return true;
	}
	public function template($param = array(),$dir = true){
		$this->init();
		$this->config->set_item('fs_vars',$param);
		$param['content'] = $this->content;
		$param['app'] = $this;
		$param['param'] = $param;
		for ($i=0; $i < count($this->assets); $i++) {
			if($dir){
				$this->load->view($this->dir_content().'/'.$this->assets[$i],$param);
			}else{
				$this->load->view($this->assets[$i],$param);
			}
			
		}
		$this->load->view("layout/".$this->layout,$param);
	}
	public function init(){
        $this->config->set_item('fs_theme_path',$this->path_theme);
		$this->config->set_item('fs_assets_path',$this->path_assets);
		$this->config->set_item('fs_title',$this->title);
	}
	public function load_view($view,$array = array()){
		$this->load->view($this->dir_content().'/'.$view,$array);

	}

    public function load_view_content($view,$array = array()){
        return $this->load->view($view, $array,TRUE);
    }
	private function dir_content(){
		$konten = explode("/", $this->content);
		if (count($konten)>0) {
			$dir = substr($this->content, 0,strlen($this->content )- strlen($konten[count($konten)-1]) - 1);
		}else{
			$dir = "";
		}
		return $dir;
	}
    public function load_asset($view,$param=array(),$current_dir = false){
        if ($current_dir){
            return $this->load->view($this->dir_content().'/'.$view,$param,TRUE);
        }else{
            return $this->load->view($view,$param,TRUE);
        }
    }
	public function get_role(){
		return $this->session->userdata($this->_ROLE);
	}
    public function empty($val){
        if ($val==null){
            return true;
        }
        if (empty(trim($val)) && $val != 0){
            return true;
        }
        return false;
    }
}

