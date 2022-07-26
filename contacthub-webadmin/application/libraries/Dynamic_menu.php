<?php
/*
* Dynmic_menu.php
*/
class Dynamic_menu {

    private $ci;            // para CodeIgniter Super Global Referencias o variables globales
    private $id_menu        = 'id="sidebar-menu" ';
    private $class_menu        = 'class="nav nav-pills nav-sidebar flex-column" data-widget="treeview" role="menu" data-accordion="false"';
    private $class_parent    = '';
    private $class_last        = 'class="last"';
    var $_DB_MERCHANT;
    // --------------------------------------------------------------------
    /**
    * PHP5        Constructor
    *
    */
    function __construct()
    {
        $this->ci =& get_instance();    // get a reference to CodeIgniter.
        $this->ci->load->model('admin/api/Api_menu');
        $this->m_menu = $this->ci->Api_menu;
        $this->current_class 	= strtolower($this->ci->router->fetch_class());
        $this->current_method = strtolower($this->ci->router->fetch_method());
        
        $this->role = $this->ci->session->userdata('role');
    }
    // --------------------------------------------------------------------
    /**
    * build_menu($table, $type)
    *
    * Description:
    *
    * builds the Dynaminc dropdown menu
    * $table allows for passing in a MySQL table name for different menu tables.
    * $type is for the type of menu to display ie; topmenu, mainmenu, sidebar menu
    * or a footer menu.
    *
    * @param    string    the MySQL database table name.
    * @param    string    the type of menu to display.
    * @return    string    $html_out using CodeIgniter achor tags.
    */

    function build_menu()
    {
        $menu = array();

        $query = $this->m_menu->getAllVisible();
        // now we will build the dynamic menus.
        $html_out = "\t\t".'<ul '.$this->class_menu.'>'."\n";

        // me despliega del query los rows de la base de datos que deseo utilizar
        foreach ($query->result as $row)
        {
            $id = isset($row->_id) ? $row->_id : null;
            $title = isset($row->Title) ? lang($row->Title) : null;
            $link_type = isset($row->LinkType) ? $row->LinkType : null;
            $module_name = isset($row->ModuleName) ? $row->ModuleName : null;
            $url = isset($row->Uri) ? $row->Uri : null;
            $position       = isset($row->Position) ? $row->Position : null;
            $target         = isset($row->Target) ? $row->Target : null;
            $parent_id      = isset($row->ParentId) ? $row->ParentId : null;
            $is_parent      = isset($row->IsParent) ? $row->IsParent : null;
            $show_menu      = isset($row->IsVisible) ? $row->IsVisible : null;
            $row->Icon      = isset($row->Icon) ? $row->Icon : null;
            // if($this->role->IsAdministrator == $row->IsAdministrator){
                
            // }else{
            //     continue;
            // }
            if ($row->Icon=='circle'){
                $icon           = 'nav-icon far fa-'.$row->Icon;
            }else{
                $icon           = 'nav-icon fas fa-'.$row->Icon;
            }


            {

                if (!empty($target)){
                    $target = 'target="'.$target.'"';
                }
                if (!empty($icon)){
                    $icon = '<i class="'.$icon.'"></i>';
                }
                if ($show_menu && $parent_id == 0)   // are we allowed to see this menu?

                {

                    if ($is_parent == TRUE)
                    {
                        // CodeIgniter's anchor(uri segments, text, attributes) tag.
                        if ($this->is_active($id)){
                            $this->class_parent = 'class="nav-item has-treeview menu-open"';
                        }else{
                            $this->class_parent = 'class="nav-item has-treeview"';
                        }
                        //$html_out .= '<li '.$this->class_parent.'>'.anchor($url, $icon.'<span>'.$title.'<i class="right fas fa-angle-left"></i></span>', array('class'=>$this->class_parent,'target'=>$target));
                        $html_out .= '<li '.$this->class_parent.'><a href="#" class="nav-link">'.$icon.' <p>'.$title.' <i class="right fas fa-angle-left"></i></p></a>';

                    }
                    else
                    {
                        if ($this->is_active($id)){
                            $this->class_parent = 'nav-link active';
                        }else{
                            $this->class_parent = 'nav-link';
                        }
                        $html_out .= "\t\t\t".'<li class="nav-item">'.anchor($url, $icon.'<p>'.$title.'</p>', array('class'=>$this->class_parent,'target'=>$target));
                    }

                }

            }
            $html_out .= $this->get_childs($id);
            // print_r($id);
        }
        // loop through and build all the child submenus.
        if(count($query->result)>0){
            $html_out .= '</li>'."\n";
        }
        $html_out .= "\t\t".'</ul>' . "\n";

        return $html_out;
    }
    private function check_parent($id){
        $query = $this->m_menu->getAllVisible(['id'=>$id]);

        if (count($query->result) > 0){
            $query = (object) $query->result[0];
            return isset($query->ParentId) ? $query->ParentId : FALSE;
        }
        return FALSE;
    }
    private function is_active($id){
        // $total_segment = $this->ci->uri->total_segments();
        // $param_current = "";
        // $uri = [];
        // for ($i=1; $i <= $total_segment; $i++) {
        //     $param_current .= $this->ci->uri->segment($i);
        //     $uri[] = $this->ci->uri->segment($i);
        //     if($i<$total_segment){

        //         $param_current .= "/";
        //     }
        //     $this->ci->db->or_where("uri",$param_current);
        // }

        // $current = strtolower($this->ci->router->fetch_class()) ;
        // $return = FALSE;
        // $query = $this->m_menu->is_active($current,$uri)->row();
        // $query2 = $this->m_menu->is_active($current,$param_current)->row();
        // if (empty($query2) == false){
        //     $query = $query2;
        // }
        $return = FALSE;
        $module_name = $this->current_class.'/'.$this->current_method;
        $query = $this->m_menu->getAllVisible(['ModuleName__icontains'=> $module_name]);

        if(count($query->result) == 0){
            $query = $this->m_menu->getAllVisible(['ModuleName__icontains'=> $this->current_class.'/index']);
        }
        if (count($query->result) > 0){
            $query = (object) $query->result[0];
            if ($id==$query->_id){
                $return = TRUE;
            }else{
                $parent_id = $query->_id;
                do{
                    $finish = FALSE;
                    $parent_id = $this->check_parent($parent_id);
                    if ($parent_id==FALSE){
                        $finish = TRUE;
                    }else if($parent_id==$id){
                        $finish = TRUE;
                        $return = TRUE;
                    }
                }while($finish == FALSE);

            }
        }
        return $return;
    }
    /**
    * get_childs($menu, $parent_id) - SEE Above Method.
    *
    * Description:
    *
    * Builds all child submenus using a recurse method call.
    *
    * @param    mixed    $id
    * @param    string    $id usuario
    * @return    mixed    $html_out if has subcats else FALSE
    */
    function get_childs($id)
    {
        $has_subcats = FALSE;

        $html_out  = '';
        //$html_out .= "\n\t\t\t\t".'<div>'."\n";
        $html_out .= "\t\t\t\t\t".'<ul class="nav nav-treeview">'."\n";

        // query q me ejecuta el submenu filtrando por usuario y para buscar el submenu segun el id que traigo
        $query = $this->m_menu->getAllVisible(['ParentId'=>$id]);

        foreach ($query->result as $row)
        {
            $id = isset($row->_id) ? $row->_id : null;
            $title = isset($row->Title) ? lang($row->Title) : null;
            $link_type = isset($row->LinkType) ? $row->LinkType : null;
            $module_name = isset($row->ModuleName) ? $row->ModuleName : null;
            $url = isset($row->Uri) ? $row->Uri : null;
            $position = isset($row->Position) ? $row->Position : nuull;
            $target = isset($row->Target) ? $row->Target : null;
            $parent_id = isset($row->ParentId) ? $row->ParentId : null;
            $is_parent = isset($row->IsParent) ? $row->IsParent : null;
            $show_menu = isset($row->IsVisible) ? $row->IsVisible : null;
            $row->Icon = isset($row->Icon) ? $row->Icon : null;
            if ($row->Icon=='circle'){
                $icon           = 'nav-icon far fa-'.$row->Icon;
            }else{
                $icon           = 'nav-icon fas fa-'.$row->Icon;
            }



            $has_subcats = TRUE;

            if (!empty($target)){
                $target = 'target="'.$target.'"';
            }
            if (!empty($icon)){
                $icon = '<i class="'.$icon.'"></i>';
            }
            if ($is_parent == TRUE)
            {
                if ($this->is_active($id)){
                    $this->class_parent = 'class="nav-item has-treeview menu-open"';
                }else{
                    $this->class_parent = 'class="nav-item has-treeview"';
                }
                // $html_out .= "\t\t\t\t\t\t".'<li '.$this->class_parent.'><a href="#"'.' name="'.$title.'" id="'.$id.'" '.$target.' >'.$icon.'<span>'.$title.'</span><span class="pull-right-container">
                //         <i class="fa fa-angle-left pull-right"></i>
                //       </span>';

            }
            else
            {
                if ($this->is_active($id)){
                    $this->class_parent = 'nav-link active';
                }else{
                    $this->class_parent = 'nav-link';
                }
                $html_out .= "\t\t\t\t\t\t".'<li class="nav-item">'.anchor($url, $icon.'<span>'.$title.'</span>', array('class'=>$this->class_parent,'target'=>$target));
            }

            // Recurse call to get more child submenus.
            $html_out .= $this->get_childs($id);
        }
        $html_out .= '</li>' . "\n";
        $html_out .= "\t\t\t\t\t".'</ul>' . "\n";
        //$html_out .= "\t\t\t\t".'</div>' . "\n";

        return ($has_subcats) ? $html_out : FALSE;

    }
}

// ------------------------------------------------------------------------
// End of Dynamic_menu Library Class.
// ------------------------------------------------------------------------
/* End of file Dynamic_menu.php */
/* Location: ../application/libraries/Dynamic_menu.php */
