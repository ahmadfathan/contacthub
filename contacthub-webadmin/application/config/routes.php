<?php
defined('BASEPATH') OR exit('No direct script access allowed');

/*
| -------------------------------------------------------------------------
| URI ROUTING
| -------------------------------------------------------------------------
| This file lets you re-map URI requests to specific controller functions.
|
| Typically there is a one-to-one relationship between a URL string
| and its corresponding controller class/method. The segments in a
| URL normally follow this pattern:
|
|	example.com/class/method/id/
|
| In some instances, however, you may want to remap this relationship
| so that a different class/function is called than the one
| corresponding to the URL.
|
| Please see the user guide for complete details:
|
|	https://codeigniter.com/user_guide/general/routing.html
|
| -------------------------------------------------------------------------
| RESERVED ROUTES
| -------------------------------------------------------------------------
|
| There are three reserved routes:
|
|	$route['default_controller'] = 'welcome';
|
| This route indicates which controller class should be loaded if the
| URI contains no data. In the above example, the "welcome" class
| would be loaded.
|
|	$route['404_override'] = 'errors/page_missing';
|
| This route will tell the Router which controller/method to use if those
| provided in the URL cannot be matched to a valid route.
|
|	$route['translate_uri_dashes'] = FALSE;
|
| This is not exactly a route, but allows you to automatically route
| controller and method names that contain dashes. '-' isn't a valid
| class or method name character, so it requires translation.
| When you set this option to TRUE, it will replace ALL dashes in the
| controller and method URI segments.
|
| Examples:	my-controller/index	-> my_controller/index
|		my-controller/my-method	-> my_controller/my_method
*/
$route['default_controller'] = 'admin/authentication/index';
$route['404_override'] = '';
$route['translate_uri_dashes'] = FALSE;

# Admin
$route['auth/login']    = 'admin/authentication/login';
$route['auth']          = 'admin/authentication/index';
$route['logout']        = 'admin/authentication/logout';

$route['dashboard']             = 'admin/dashboard/index';

$route['withdraw']                      = 'admin/withdraw/index';
$route['withdraw/ajax_list']                      = 'admin/withdraw/ajax_list';
$route['withdraw/edit/(:any)']     = 'admin/withdraw/edit/$1';
$route['withdraw/update/(:any)']     = 'admin/withdraw/update/$1';

$route['credit']                        = 'admin/credit/index';
$route['credit/ajax_list']              = 'admin/credit/ajax_list';
$route['credit/ajax_list/view']         = 'admin/credit/ajax_list_view';
$route['credit/add']                    = 'admin/credit/add';
$route['credit/create']                 = 'admin/credit/create';
$route['credit/view/(:any)']            = 'admin/credit/view/$1';
$route['credit/edit/(:any)/(:any)']     = 'admin/credit/edit/$1/$2';
$route['credit/update/(:any)/(:any)']   = 'admin/credit/update/$1/$2';
$route['credit/delete/(:any)/(:any)']   = 'admin/credit/delete/$1/$2';

$route['role']                          = 'admin/role/index';
$route['role/ajax_list']                = 'admin/role/ajax_list';
$route['role/add']                      = 'admin/role/add';
$route['role/edit/(:any)']              = 'admin/role/edit/$1';
$route['role/update/(:any)']            = 'admin/role/update/$1';
$route['role/delete/(:any)']            = 'admin/role/delete/$1';
$route['role/create']                   = 'admin/role/create';


$route['notification']                        = 'admin/notification/index';
$route['notification/ajax_list']              = 'admin/notification/ajax_list';
$route['notification/delete/(:any)']          = 'admin/notification/delete/$1';
$route['notification/add']                    = 'admin/notification/add';
$route['notification/create']                 = 'admin/notification/create';
$route['notification/edit/(:any)']     = 'admin/notification/edit/$1';
$route['notification/update/(:any)']   = 'admin/notification/update/$1';


$route['businesstype']                        = 'admin/businesstype/index';
$route['businesstype/ajax_list']              = 'admin/businesstype/ajax_list';
$route['businesstype/add']              = 'admin/businesstype/add';
$route['businesstype/delete/(:any)']              = 'admin/businesstype/delete/$1';
$route['businesstype/create']              = 'admin/businesstype/create';


$route['profession']                       = 'admin/profesi/index';
$route['profession/ajax_list']             = 'admin/profesi/ajax_list';
$route['profession/add']                   = 'admin/profesi/add';
$route['profession/delete/(:any)']         = 'admin/profesi/delete/$1';
$route['profession/create']                = 'admin/profesi/create';



$route['interest']                       = 'admin/interest/index';
$route['interest/ajax_list']             = 'admin/interest/ajax_list';
$route['interest/add']                   = 'admin/interest/add';
$route['interest/delete/(:any)']         = 'admin/interest/delete/$1';
$route['interest/create']                = 'admin/interest/create';

$route['greeting']                       = 'admin/greeting/index';
$route['greeting/ajax_list']             = 'admin/greeting/ajax_list';
$route['greeting/add']                   = 'admin/greeting/add';
$route['greeting/delete/(:any)']         = 'admin/greeting/delete/$1';
$route['greeting/create']                = 'admin/greeting/create';


$route['article']                       = 'admin/article/index';
$route['article/ajax_list']             = 'admin/article/ajax_list';
$route['article/add']                   = 'admin/article/add';
$route['article/edit/(:any)']           = 'admin/article/edit/$1';
$route['article/update/(:any)']         = 'admin/article/update/$1';
$route['article/delete/(:any)']         = 'admin/article/delete/$1';
$route['article/create']                = 'admin/article/create';

$route['settings']                      = 'admin/settings/index';
$route['settings/ajax_list']            = 'admin/settings/ajax_list';
$route['settings/edit/(:any)']          = 'admin/settings/edit/$1';
$route['settings/update/(:any)']        = 'admin/settings/update/$1';


$route['pengguna']                          = 'admin/user/index';
$route['pengguna/ajax_list']                = 'admin/user/ajax_list';
$route['pengguna/add']                      = 'admin/user/add';
$route['pengguna/edit/(:any)']              = 'admin/user/edit/$1';
$route['pengguna/update/(:any)']            = 'admin/user/update/$1';
$route['pengguna/delete/(:any)']            = 'admin/user/delete/$1';
$route['pengguna/create']                   = 'admin/user/create';


$route['profile']                          = 'admin/profile/index';
$route['profile/ubahpassword']             = 'admin/profile/ubahpassword';
$route['profile/update']                   = 'admin/profile/update';
$route['profile/update/password']          = 'admin/profile/updatepassword';

$route['customer']                          = 'admin/customer/index';
$route['customer/ajax_list']                = 'admin/customer/ajax_list';
$route['customer/ajax_list_downline']                = 'admin/customer/ajax_list_downline';
$route['customer/ajax_list_save']                = 'admin/customer/ajax_list_save';
$route['customer/ajax_list_share']                = 'admin/customer/ajax_list_share';
$route['customer/ajax_list_penghasilan']                = 'admin/customer/ajax_list_penghasilan';
$route['customer/add']                      = 'admin/customer/add';
$route['customer/edit/(:any)']              = 'admin/customer/edit/$1';
$route['customer/update/(:any)']            = 'admin/customer/update/$1';
$route['customer/delete/(:any)']            = 'admin/customer/delete/$1';
$route['customer/create']                   = 'admin/customer/create';
$route['customer/view/(:any)']              = 'admin/customer/view/$1';


$route['affiliate']                          = 'admin/affiliate/index';
$route['affiliate/ajax_list']                = 'admin/affiliate/ajax_list';
$route['affiliate/ajax_list']                = 'admin/affiliate/ajax_list';

$route['penghasilan']                          = 'admin/penghasilan/index';
$route['penghasilan/ajax_list']                = 'admin/penghasilan/ajax_list';

$route['autosave']                          = 'admin/autosave/index';
$route['autosave/run']                      = 'admin/autosave/run';



$route['feed']                       = 'admin/feed/index';
$route['feed/ajax_list']             = 'admin/feed/ajax_list';
$route['feed/add']                   = 'admin/feed/add';
$route['feed/edit/(:any)']           = 'admin/feed/edit/$1';
$route['feed/update/(:any)']         = 'admin/feed/update/$1';
$route['feed/delete/(:any)']         = 'admin/feed/delete/$1';
$route['feed/create']                = 'admin/feed/create';
$route['feed/approve/(:any)']        = 'admin/feed/approve/$1';
$route['feed/reject/(:any)']         = 'admin/feed/reject/$1';


$route['slider']                       = 'admin/slider/index';
$route['slider/ajax_list']             = 'admin/slider/ajax_list';
$route['slider/add']                   = 'admin/slider/add';
$route['slider/edit/(:any)']           = 'admin/slider/edit/$1';
$route['slider/update/(:any)']         = 'admin/slider/update/$1';
$route['slider/delete/(:any)']         = 'admin/slider/delete/$1';
$route['slider/create']                = 'admin/slider/create';